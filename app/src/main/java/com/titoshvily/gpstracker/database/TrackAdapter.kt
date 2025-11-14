package com.titoshvily.gpstracker.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.titoshvily.gpstracker.R
import com.titoshvily.gpstracker.databinding.TrackItemBinding

class TrackAdapter(val listener: Listener) : ListAdapter<TrackItem, TrackAdapter.Holder>(Holder.Comparator()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }


    class Holder(view: View, private val listener: Listener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val binding = TrackItemBinding.bind(view)
        private var trackTemp: TrackItem? = null
        init {
            binding.ibDelete.setOnClickListener(this)
            binding.item.setOnClickListener(this)
        }
        fun bind(trackItem: TrackItem) = with(binding) {

            trackTemp = trackItem
            val speed = "${trackItem.velocity} km/h"
            val time = "${trackItem.time}"
            val distance = "${trackItem.distance} km"

            tvtime.text = trackItem.date
            tvSpeed.text = speed
            tvtime.text = time
            tvDistance.text = distance

        }

        override fun onClick(view: View?) {
           val type =  when(view?.id){
                R.id.ibDelete -> ClickType.DELETE
                R.id.item -> ClickType.OPEN
               else -> ClickType.OPEN
            }
            trackTemp?.let { listener.onClick(it, type) }
        }

        class Comparator : DiffUtil.ItemCallback<TrackItem>() {

            override fun areItemsTheSame(
                oldItem: TrackItem,
                newItem: TrackItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: TrackItem,
                newItem: TrackItem
            ): Boolean {
                return oldItem == newItem
            }

        }


    }


    interface Listener{
        fun onClick(track: TrackItem, type: ClickType)
    }


    enum class ClickType{
        DELETE,
        OPEN
    }
}
