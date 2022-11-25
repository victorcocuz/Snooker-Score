package com.quickpoint.snookerboard.fragments.postgame


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quickpoint.snookerboard.databinding.LayoutPostGameStatsViewBinding
import com.quickpoint.snookerboard.domain.DomainScore

class PostGameAdapter:
    ListAdapter<Pair<DomainScore, DomainScore>, PostGameAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class ViewHolder private constructor(private val binding: LayoutPostGameStatsViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(frameScores: Pair<DomainScore, DomainScore>, position: Int) {
            binding.apply {
                varBgType = position % 2
                frameScoreA = frameScores.first
                frameScoreB = frameScores.second
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = LayoutPostGameStatsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Pair<DomainScore, DomainScore>>() {
        override fun areItemsTheSame(oldItem: Pair<DomainScore, DomainScore>, newItem: Pair<DomainScore, DomainScore>): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Pair<DomainScore, DomainScore>, newItem: Pair<DomainScore, DomainScore>): Boolean {
            return oldItem.first.frameId == newItem.first.frameId
        }
    }
}