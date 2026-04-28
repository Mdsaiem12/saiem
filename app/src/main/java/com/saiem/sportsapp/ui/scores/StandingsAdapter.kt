package com.saiem.sportsapp.ui.scores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saiem.sportsapp.R
import com.saiem.sportsapp.data.model.Standing
import com.saiem.sportsapp.databinding.ItemStandingBinding

class StandingsAdapter : ListAdapter<Standing, StandingsAdapter.StandingViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandingViewHolder {
        val binding = ItemStandingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StandingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StandingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StandingViewHolder(private val binding: ItemStandingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(standing: Standing) {
            binding.apply {
                tvPosition.text = standing.position.toString()
                tvTeamName.text = standing.team.name
                tvPlayed.text = standing.played.toString()
                tvWon.text = standing.won.toString()
                tvDrawn.text = standing.drawn.toString()
                tvLost.text = standing.lost.toString()
                tvGD.text = if (standing.goalDifference >= 0)
                    "+${standing.goalDifference}" else standing.goalDifference.toString()
                tvPoints.text = standing.points.toString()

                Glide.with(itemView).load(standing.team.logo)
                    .placeholder(R.drawable.ic_team_placeholder).into(ivTeamLogo)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Standing>() {
        override fun areItemsTheSame(old: Standing, new: Standing) =
            old.team.id == new.team.id
        override fun areContentsTheSame(old: Standing, new: Standing) = old == new
    }
}
