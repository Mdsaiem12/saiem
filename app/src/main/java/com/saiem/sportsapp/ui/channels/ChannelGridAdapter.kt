package com.saiem.sportsapp.ui.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saiem.sportsapp.R
import com.saiem.sportsapp.data.model.TvChannel
import com.saiem.sportsapp.databinding.ItemChannelGridBinding

class ChannelGridAdapter(
    private val onClick: (TvChannel) -> Unit
) : ListAdapter<TvChannel, ChannelGridAdapter.ChannelViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChannelViewHolder(private val binding: ItemChannelGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: TvChannel) {
            binding.tvChannelName.text = channel.name
            Glide.with(itemView)
                .load(channel.logo)
                .placeholder(R.drawable.ic_tv_placeholder)
                .into(binding.ivChannelLogo)

            binding.tvQuality.text = channel.quality.name
            binding.root.setOnClickListener { onClick(channel) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TvChannel>() {
        override fun areItemsTheSame(old: TvChannel, new: TvChannel) = old.id == new.id
        override fun areContentsTheSame(old: TvChannel, new: TvChannel) = old == new
    }
}
