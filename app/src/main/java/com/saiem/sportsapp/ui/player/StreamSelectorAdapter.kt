package com.saiem.sportsapp.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saiem.sportsapp.data.model.StreamSource
import com.saiem.sportsapp.databinding.ItemStreamSourceBinding

class StreamSelectorAdapter(
    private val sources: List<StreamSource>,
    private val onSelect: (StreamSource) -> Unit
) : RecyclerView.Adapter<StreamSelectorAdapter.SourceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val binding = ItemStreamSourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        holder.bind(sources[position])
    }

    override fun getItemCount() = sources.size

    inner class SourceViewHolder(private val binding: ItemStreamSourceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(source: StreamSource) {
            binding.tvSourceTitle.text = source.title.ifBlank { "Source ${bindingAdapterPosition + 1}" }
            binding.tvQuality.text = source.quality.name
            binding.tvLanguage.text = source.language
            binding.root.setOnClickListener { onSelect(source) }
        }
    }
}
