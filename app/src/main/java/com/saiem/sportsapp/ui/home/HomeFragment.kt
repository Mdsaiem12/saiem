package com.saiem.sportsapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saiem.sportsapp.data.model.Match
import com.saiem.sportsapp.data.model.Resource
import com.saiem.sportsapp.data.model.SportType
import com.saiem.sportsapp.databinding.FragmentHomeBinding
import com.saiem.sportsapp.ui.player.VideoPlayerActivity
import com.saiem.sportsapp.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var matchAdapter: MatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSportTabs()
        observeViewModel()
        setupRefresh()
    }

    private fun setupRecyclerView() {
        matchAdapter = MatchAdapter { match -> openPlayer(match) }
        binding.rvMatches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = matchAdapter
        }
    }

    private fun openPlayer(match: Match) {
        val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
            putExtra(VideoPlayerActivity.EXTRA_MATCH_ID, match.id)
            putExtra(
                VideoPlayerActivity.EXTRA_MATCH_TITLE,
                "${match.homeTeam.name} vs ${match.awayTeam.name}"
            )
        }
        startActivity(intent)
    }

    private fun setupSportTabs() {
        binding.chipFootball.setOnClickListener {
            viewModel.selectSport(SportType.FOOTBALL)
        }
        binding.chipCricket.setOnClickListener {
            viewModel.selectSport(SportType.CRICKET)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.liveFootball.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> showLoading(true)
                            is Resource.Success -> {
                                showLoading(false)
                                matchAdapter.submitList(resource.data)
                                binding.tvNoMatches.isVisible = resource.data.isEmpty()
                            }
                            is Resource.Error -> {
                                showLoading(false)
                                binding.tvNoMatches.isVisible = true
                                binding.tvNoMatches.text = resource.message
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadLiveMatches()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun showLoading(show: Boolean) {
        binding.shimmerLayout.isVisible = show
        if (show) binding.shimmerLayout.startShimmer()
        else binding.shimmerLayout.stopShimmer()
        binding.rvMatches.isVisible = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
