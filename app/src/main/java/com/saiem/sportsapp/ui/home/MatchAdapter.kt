package com.saiem.sportsapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saiem.sportsapp.R
import com.saiem.sportsapp.data.model.Match
import com.saiem.sportsapp.data.model.MatchStatus
import com.saiem.sportsapp.databinding.ItemMatchBinding
import java.text.SimpleDateFormat
import java.util.*

class MatchAdapter(
    private val onMatchClick: (Match) -> Unit
) : ListAdapter<Match, MatchAdapter.MatchViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {
            binding.apply {
                tvHomeTeam.text = match.homeTeam.name
                tvAwayTeam.text = match.awayTeam.name
                tvHomeScore.text = match.homeScore.toString()
                tvAwayScore.text = match.awayScore.toString()
                tvTournament.text = match.tournament

                Glide.with(itemView).load(match.homeTeam.logo)
                    .placeholder(R.drawable.ic_team_placeholder).into(ivHomeLogo)
                Glide.with(itemView).load(match.awayTeam.logo)
                    .placeholder(R.drawable.ic_team_placeholder).into(ivAwayLogo)

                when (match.status) {
                    MatchStatus.LIVE -> {
                        tvStatus.text = if (match.minute > 0) "${match.minute}'" else "LIVE"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.live_red)
                        )
                        ivLiveDot.isVisible = true
                    }
                    MatchStatus.HALF_TIME -> {
                        tvStatus.text = "HT"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.orange)
                        )
                        ivLiveDot.isVisible = false
                    }
                    MatchStatus.FINISHED -> {
                        tvStatus.text = "FT"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.text_secondary)
                        )
                        ivLiveDot.isVisible = false
                    }
                    else -> {
                        tvStatus.text = formatTime(match.startTime)
                        tvStatus.setTextColor(
                            ContextCompat.getColor(itemView.context, R.color.text_secondary)
                        )
                        ivLiveDot.isVisible = false
                    }
                }

                btnWatch.isVisible = match.hasStreams || match.status == MatchStatus.LIVE
                btnWatch.setOnClickListener { onMatchClick(match) }
                root.setOnClickListener { onMatchClick(match) }
            }
        }

        private fun formatTime(timestamp: Long): String {
            if (timestamp == 0L) return "TBD"
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Match, newItem: Match) = oldItem == newItem
    }
}
