package  com.example.swiperecyclerviewitemdemo.swipeHelper

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.swiperecyclerviewitemdemo.R
import com.example.swiperecyclerviewitemdemo.Utils
import com.geokey.custom.swipeHelper.FullSwipeListener
import java.util.*

abstract class SwipeHelper(
    val recyclerView: RecyclerView,
    private val fullSwipeListener: FullSwipeListener,

    ) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.ACTION_STATE_IDLE) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return false
    }

    private val context: Context = recyclerView.context
    private var buttonList: MutableList<SwipeButtons>? = null
    private lateinit var gestureDetector: GestureDetector

    private var swipePosition = -1

    private var swipeTrashHold = .5f
    private val buttonBuffer: MutableMap<Int, MutableList<SwipeButtons>>
    private lateinit var removeQueue: LinkedList<Int>
    private var previousItem = -1
    abstract fun instantiateMyButton(
        viewHolder: ViewHolder,
        buffer: MutableList<SwipeButtons>,
        fullSwipeText: MutableList<String>
    )

    private var isAnyItemActive = false
    private var currentViewHolder: ViewHolder? = null

    private var currentDx = 0f
    private var fullSwipeText: MutableList<String> = ArrayList()

    companion object {
        var inFullSwipeMode = false
        fun getScreenWidth(): Int {
            return Resources.getSystem().displayMetrics.widthPixels
        }
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (currentViewHolder?.itemView?.translationX!! > 5f) {
                for (button in buttonList!!) {
                    if (button.onClick(e.x, e.y)) {
                        return true
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    recyclerView.adapter?.notifyItemChanged(swipePosition)
                }, 50)
            }
            return true
        }
    }

    private val onTouchListener = View.OnTouchListener { view, motionEvent ->
        view.performClick()
        if (swipePosition < 0) return@OnTouchListener false
        val point = Point(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())
        val swipeViewHolder = recyclerView.findViewHolderForAdapterPosition(swipePosition)

        val swipeItem = swipeViewHolder?.itemView
        val rect = Rect()
        swipeItem?.getGlobalVisibleRect(rect)

        if (motionEvent.action == MotionEvent.ACTION_DOWN || motionEvent.action == MotionEvent.ACTION_MOVE || motionEvent.action == MotionEvent.ACTION_UP) {
            if (rect.top < point.y && rect.bottom > point.y) {
                gestureDetector.onTouchEvent(motionEvent)
            } else {
                removeQueue.add(swipePosition)
                swipePosition = -1

                if (!isAnyItemActive) {
                    recyclerView.adapter?.notifyItemChanged(previousItem)
                    gestureDetector.onTouchEvent(motionEvent)
                }
            }
        }
        false
    }

    @Synchronized
    private fun recoverSwipeItem() {
        while (!removeQueue.isEmpty()) {
            val pos = removeQueue.poll()!!.toInt()
            if (pos > -1) {
                recyclerView.adapter?.notifyItemChanged(pos)
            }
        }
    }

    init {
        this.buttonList = ArrayList()
        this.gestureDetector = GestureDetector(context, gestureListener)
        this.recyclerView.setOnTouchListener(onTouchListener)
        this.buttonBuffer = HashMap()
        this.removeQueue = IntLinkedList()
        attachSwipe()
    }

    private fun attachSwipe() {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    class IntLinkedList : LinkedList<Int>() {

        override fun lastIndexOf(element: Int): Int {
            return element
        }

        override fun remove(element: Int): Boolean {
            return false
        }

        override fun indexOf(element: Int): Int {
            return element
        }

        override fun add(element: Int): Boolean {
            return if (contains(element))
                false
            else return super.add(element)
        }
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition
        previousItem = pos
        if (swipePosition != pos)
            removeQueue.add(swipePosition)
        swipePosition = pos

        if (buttonBuffer.contains(swipePosition))
            buttonList = buttonBuffer[swipePosition]
        else
            buttonList!!.clear()
        buttonBuffer.clear()
        swipeTrashHold = 0.5f
        recoverSwipeItem()
    }

    override fun getSwipeThreshold(viewHolder: ViewHolder): Float {
        return swipeTrashHold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        currentDx = dX
        val pos = viewHolder.adapterPosition
        var translationX = dX
        val itemView = viewHolder.itemView
        if (pos < 0) {
//            swipePosition = pos
            return
        }
        isAnyItemActive = isCurrentlyActive
        currentViewHolder = viewHolder

        if (inFullSwipeMode) {
            drawBackground(c, itemView, viewHolder, translationX)
            translationX = c.width.toFloat() - 1
        } else {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (dX > 0) {

                    val buffer: MutableList<SwipeButtons> = ArrayList()
                    instantiateMyButton(viewHolder, buffer, fullSwipeText)
                    buttonBuffer[pos] = buffer

                    /*Change this calculation if you want to give more space to button instead of half of screen width*/
                    translationX =
                        dX * (getScreenWidth().div(2f)) / itemView.width

                    if (dX > c.width.toFloat())
                        translationX += (dX - c.width)
                    if (!isCurrentlyActive && translationX > c.width * .90) {
                        if (!inFullSwipeMode) {
                            inFullSwipeMode = true
                            fullSwipeListener.onFullSwipe(pos)
                            translationX = c.width.toFloat() - 5
                            drawBackground(c, itemView, viewHolder, translationX)
                        }
                    }

                    if ((translationX > c.width / 2)) {
                        if (translationX > c.width - 50) {
                            translationX = c.width.toFloat() - 5
                        }
                        drawBackground(c, itemView, viewHolder, translationX)
                    } else {
                        if (translationX > 5f)
                            drawButton(c, itemView, buffer, pos, translationX)
                    }
                }
            }
            if (translationX > c.width)
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    c.width.toFloat(),
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            else
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    translationX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
        }
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            translationX,
            dY,
            actionState,
            !isCurrentlyActive
        )
    }

    /***
     * It will draw canvas for full swipe feature.
     */
    private fun drawBackground(
        c: Canvas,
        itemView: View,
        viewHolder: ViewHolder,
        translationX: Float
    ) {
        val paintText = Paint()
        paintText.color =
            Color.WHITE
        paintText.textSize =
            14f * context.resources?.displayMetrics?.density!!
        paintText.textAlign = Paint.Align.CENTER

        /*Draw background*/
        val p = Paint()
        p.color = ContextCompat.getColor(viewHolder.itemView.context, R.color.colorPrimary)
        p.color
        c.drawRect(
            Rect(
                itemView.left,
                itemView.top,
                translationX.toInt(),
                itemView.bottom
            ), p
        )
        val unlockBitmap = BitmapFactory.decodeResource(
            context.resources, R.drawable.swipe_right
        )
        val resizedBitmap = Bitmap.createScaledBitmap(
            unlockBitmap,
            Utils.convertAsPerDeviceDensity(context, 28).toInt(),
            Utils.convertAsPerDeviceDensity(context, 28).toInt(),
            false
        )
        /*Draw Bitmap or Icon on canvas */
        c.drawBitmap(
            resizedBitmap,
            (itemView.left + itemView.right - resizedBitmap.width) / 2f,
            (itemView.top + itemView.bottom - resizedBitmap.height) / 2f,
            p
        )
        /*Draw text below icon.*/
        c.drawText(
            fullSwipeText[0],
            (itemView.width / 2).toFloat(),
            (itemView.top + itemView.bottom).toFloat() / 2 + resizedBitmap.height,
            paintText
        )
    }

    /***
     * Draw Button when Item half swiped
     */
    private fun drawButton(
        c: Canvas,
        itemView: View,
        buffer: MutableList<SwipeButtons>,
        pos: Int,
        translationX: Float
    ) {
        var left = itemView.left.toFloat()
        val dButtonWidth = (1 * translationX) / buffer.size
        for (button in buffer) {
            val right = left + dButtonWidth
            button.run {
                onDraw(
                    c,
                    RectF(/* left = */ right, /* top = */
                        itemView.top.toFloat(), /* right = */
                        left, /* bottom = */
                        itemView.bottom.toFloat()
                    ),
                    pos
                )
            }
            left = right
        }
    }


}
