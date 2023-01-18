package com.example.swiperecyclerviewitemdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swiperecyclerviewitemdemo.databinding.ActivityMainBinding
import com.geokey.custom.swipeHelper.FullSwipeListener
import com.example.swiperecyclerviewitemdemo.swipeHelper.SwipeButtons
import com.geokey.custom.swipeHelper.SwipeClickListener
import com.example.swiperecyclerviewitemdemo.swipeHelper.SwipeHelper

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializing()

        /*Set Swipe listener*/
        object : SwipeHelper(
            recyclerView = binding.recycleView, fullSwipeListener = fullSwipeListener
        ) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<SwipeButtons>,
                fullSwipeText: MutableList<String>
            ) {
                /*- Add buttons to swipe item*/
                addButtons(buffer)

                /*Text you want to show in full swipe below icon*/
                fullSwipeText.add(0, "Full Swipe Text")
            }
        }
    }

    private fun initializing() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /*Initializing recycleView*/
        binding.recycleView.layoutManager = LinearLayoutManager(this)
        binding.recycleView.adapter = CustomAdapter()
    }

    private fun addButtons(buffer: MutableList<SwipeButtons>) {
        buffer.add(swipeButton(label = "Option 1", drawable = R.drawable.one))
        buffer.add(swipeButton("Option 2", R.drawable.two))
        buffer.add(swipeButton("Option 3", R.drawable.three))

    }

    private var fullSwipeListener = object : FullSwipeListener {
        override fun onFullSwipe(position: Int) {
            binding.recycleView.adapter?.notifyItemChanged(position)

            if (SwipeHelper.inFullSwipeMode) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        Toast.makeText(
                            this@MainActivity,
                            "Full Swipe for Position :- $position",
                            Toast.LENGTH_SHORT
                        ).show()
                        SwipeHelper.inFullSwipeMode = false
                    },
                    500
                )
            }

        }
    }

    private fun swipeButton(label: String, drawable: Int): SwipeButtons {
        val bitmap = BitmapFactory.decodeResource(
            resources, drawable
        )
        /*Resize the bitmap in proper height width*/
        val resizedBitmap =
            try {
                Bitmap.createScaledBitmap(
                    bitmap,
                    Utils.convertAsPerDeviceDensity(this, 28).toInt(),
                    Utils.convertAsPerDeviceDensity(this, 28).toInt(),
                    false
                )
            } catch (e: Exception) {
                null
            }
        return SwipeButtons(context = this,
            text = label,
            textSize = 10,
            imgBitmap = resizedBitmap,
            color = ContextCompat.getColor(this, R.color.colorPrimary),
            listener = object : SwipeClickListener {
                override fun onClick(position: Int) {
                    binding.recycleView.adapter?.notifyItemChanged(position)
                    Toast.makeText(
                        this@MainActivity,
                        "$label :- Position $position",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}