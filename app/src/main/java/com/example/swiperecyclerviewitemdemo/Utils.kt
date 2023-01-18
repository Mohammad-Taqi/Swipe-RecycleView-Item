package com.example.swiperecyclerviewitemdemo

import android.content.Context
import android.util.Log

object  Utils {
    fun convertAsPerDeviceDensity(context: Context?, value: Int): Float {
        return value * context?.resources?.displayMetrics?.density!!
    }
}