package com.example.swiperecyclerviewitemdemo.swipeHelper

import android.content.Context
import android.graphics.*
import com.geokey.custom.swipeHelper.SwipeClickListener

class SwipeButtons(
    private val context: Context?,
    private val text: String,
    private val textSize: Int,
    private val imgBitmap: Bitmap?,
    private val color: Int,
    private val listener: SwipeClickListener
) {
    private var position: Int = 0
    private var clickRegion: RectF? = null

    /***
     * click listener for swipe Button.
     * Check if clicked is on that region or not.
     */
    fun onClick(x: Float, y: Float): Boolean {
        clickRegion?.let {
            if ( (x in (it.right..it.left)) && (y in (it.top..it.bottom))) {
                listener.onClick(position)
                return true
            }
        }

        return false
    }

    fun onDraw(canvas: Canvas, rectF: RectF, position: Int) {
        val paint = Paint()
        paint.color = color
        rectF.set(rectF.left, rectF.top, rectF.right - 4, rectF.bottom)
        canvas.drawRect(rectF, paint)
        paint.color = Color.WHITE
        /*Drawing line between two rect(i.e button)*/
        canvas.drawRect(rectF.right - 4, rectF.top, rectF.right, rectF.bottom, paint)
        /*if we pass null in bitmap than the text will be shown*/
        paint.color = Color.BLACK
        paint.textSize = textSize.toFloat()

        val r = Rect()
        val cHeight = rectF.height()
        val cWidth = rectF.width()
        paint.textAlign = Paint.Align.CENTER
        paint.getTextBounds(text, 0, text.length, r)

        if (imgBitmap == null) {
            val x: Float = cWidth / 2f - r.width() / 2f - r.left.toFloat()
            val y: Float = cHeight / 2f - r.height() / 2f - r.bottom.toFloat()
            canvas.drawText(text, rectF.left + x, rectF.top + y, paint)
        } else {
            /*Draw bitmap*/
            canvas.drawBitmap(
                imgBitmap,
                (rectF.left + rectF.right - imgBitmap.width) / 2,
                (rectF.top + rectF.bottom - imgBitmap.height) / 2,
                paint
            )

            /*Draw text below bitmap*/
            paint.color = Color.WHITE
            paint.textSize = 12f * context?.resources?.displayMetrics?.density!!
            canvas.drawText(
                text,
                ((rectF.left + rectF.right) / 2),
//                (rectF.bottom - 12 * (context.resources?.displayMetrics?.density!!).toInt()),
                ((rectF.top + rectF.bottom + imgBitmap.height) / 2) + (paint.textSize),
                paint
            )
        }
        clickRegion = rectF
        this.position = position
    }
}
