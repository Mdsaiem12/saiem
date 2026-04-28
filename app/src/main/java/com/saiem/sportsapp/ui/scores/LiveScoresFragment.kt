package com.saiem.sportsapp.ui.scores

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
import com.saiem.sportsapp.data.model.Resource
import com.saiem.sportsapp.databinding.FragmentLiveScoresBinding
import com.saiem.sportsapp.viewmodel.LiveScoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LiveScoresFragment : Fragment() {

    private var _binding: FragmentLiveScoresBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LiveScoreViewModel by viewModels()
    private lateinit var standingsAdapter: StandingsAdapter
    private lateinit var competitionAdapter: CompetitionChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveScoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCompetitionChips()
        setupStandingsRecycler()
        observeViewModel()
    }

    private fun setupCompetitionChips() {
        competitionAdapter = CompetitionChipAdapter(viewModel.competitions) { competition ->
            viewModel.loadStandings(competition.code)
        }
        binding.rvCompetitions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = competitionAdapter
        }
        // Load default
        viewModel.loadStandings("PL")
    }

    private fun setupStandingsRecycler() {
        standingsAdapter = StandingsAdapter()
        binding.rvStandings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = standingsAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.standings.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.progressBar.isVisible = true
                            }
                            is Resource.Success -> {
                                binding.progressBar.isVisible = false
                                standingsAdapter.submitList(resource.data.standings)
                                binding.tvLeagueName.text = resource.data.leagueName
                            }
                            is Resource.Error -> {
                                binding.progressBar.isVisible = false
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
