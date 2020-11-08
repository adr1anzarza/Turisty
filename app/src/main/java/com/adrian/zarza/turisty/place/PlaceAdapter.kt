package com.adrian.zarza.turisty.place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adrian.zarza.turisty.database.Place
import com.adrian.zarza.turisty.databinding.ListItemPlaceBinding

class PlaceAdapter(val clickListener: PlaceListener) : ListAdapter<Place, PlaceAdapter.ViewHolder>(PlaceDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = getItem(position)
        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)

    }

    class ViewHolder private constructor(val binding: ListItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sleepNightListener: PlaceListener, item: Place) {
            binding.task = item
            binding.clickListener = sleepNightListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemPlaceBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }

    class PlaceListener(val clickListener: (taskId: Long) -> Unit) {
        fun onClick(task: Place) = clickListener(task.placeId)
    }

}

