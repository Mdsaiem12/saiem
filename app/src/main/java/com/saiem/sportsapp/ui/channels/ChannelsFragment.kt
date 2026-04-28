package com.saiem.sportsapp.ui.channels

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
import androidx.recyclerview.widget.GridLayoutManager
import com.saiem.sportsapp.data.model.Resource
import com.saiem.sportsapp.databinding.FragmentChannelsBinding
import com.saiem.sportsapp.ui.player.VideoPlayerActivity
import com.saiem.sportsapp.viewmodel.StreamViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StreamViewModel by viewModels()
    private lateinit var channelAdapter: ChannelGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGrid()
        viewModel.loadChannels()
        observeViewModel()
    }

    private fun setupGrid() {
        channelAdapter = ChannelGridAdapter { channel ->
            val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
                putExtra(VideoPlayerActivity.EXTRA_STREAM_URL, channel.streamUrl)
                putExtra(VideoPlayerActivity.EXTRA_MATCH_TITLE, channel.name)
                putExtra(VideoPlayerActivity.EXTRA_MATCH_ID, channel.id)
            }
            startActivity(intent)
        }
        binding.rvChannels.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = channelAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.channels.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.shimmerGrid.isVisible = true
                            binding.shimmerGrid.startShimmer()
                            binding.rvChannels.isVisible = false
                        }
                        is Resource.Success -> {
                            binding.shimmerGrid.stopShimmer()
                            binding.shimmerGrid.isVisible = false
                            binding.rvChannels.isVisible = true
                            channelAdapter.submitList(resource.data)
                        }
                        is Resource.Error -> {
                            binding.shimmerGrid.stopShimmer()
                            binding.shimmerGrid.isVisible = false
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
