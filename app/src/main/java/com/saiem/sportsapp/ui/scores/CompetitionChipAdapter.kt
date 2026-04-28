package com.saiem.sportsapp.ui.scores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saiem.sportsapp.databinding.ItemCompetitionChipBinding
import com.saiem.sportsapp.viewmodel.Competition

class CompetitionChipAdapter(
    private val competitions: List<Competition>,
    private val onSelect: (Competition) -> Unit
) : RecyclerView.Adapter<CompetitionChipAdapter.ChipViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val binding = ItemCompetitionChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(competitions[position], position == selectedPosition)
    }

    override fun getItemCount() = competitions.size

    inner class ChipViewHolder(private val binding: ItemCompetitionChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(competition: Competition, isSelected: Boolean) {
            binding.chip.text = "${competition.flag} ${competition.name}"
            binding.chip.isChecked = isSelected
            binding.chip.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onSelect(competition)
            }
        }
    }
}
