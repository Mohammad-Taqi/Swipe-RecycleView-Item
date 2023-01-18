package com.example.swiperecyclerviewitemdemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.swiperecyclerviewitemdemo.databinding.ItemViewBinding

class CustomAdapter : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txt.text = "Item Number :: $position"
    }

    override fun getItemCount(): Int {
        return 30
    }

}
