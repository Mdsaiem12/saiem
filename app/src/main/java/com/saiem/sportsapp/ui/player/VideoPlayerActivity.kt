package com.saiem.sportsapp.ui.player

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.saiem.sportsapp.data.model.Resource
import com.saiem.sportsapp.data.model.StreamSource
import com.saiem.sportsapp.databinding.ActivityVideoPlayerBinding
import com.saiem.sportsapp.viewmodel.StreamViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private val viewModel: StreamViewModel by viewModels()
    private var player: ExoPlayer? = null

    @Inject lateinit var okHttpClient: OkHttpClient

    companion object {
        const val EXTRA_MATCH_ID = "match_id"
        const val EXTRA_MATCH_TITLE = "match_title"
        const val EXTRA_STREAM_URL = "stream_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: ""
        val matchTitle = intent.getStringExtra(EXTRA_MATCH_TITLE) ?: ""
        val directStreamUrl = intent.getStringExtra(EXTRA_STREAM_URL)

        binding.tvMatchTitle.text = matchTitle

        setupPlayer()
        setupControls()

        if (!directStreamUrl.isNullOrBlank()) {
            playStream(directStreamUrl)
        } else {
            viewModel.loadStreams(matchId, matchTitle)
            observeStreams()
        }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.playerView.setFullscreenButtonClickListener { isFullscreen ->
            requestedOrientation = if (isFullscreen)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                binding.progressBar.isVisible = state == Player.STATE_BUFFERING
                binding.tvBuffering.isVisible = state == Player.STATE_BUFFERING
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                binding.tvError.isVisible = true
                binding.tvError.text = "Playback error. Trying next source…"
                viewModel.streams.value.let { resource ->
                    if (resource is Resource.Success) {
                        val sources = resource.data
                        val currentUrl = (player?.currentMediaItem?.localConfiguration?.uri?.toString() ?: "")
                        val nextSource = sources.firstOrNull { it.streamUrl != currentUrl }
                        if (nextSource != null) {
                            binding.tvError.postDelayed({
                                binding.tvError.isVisible = false
                                playStream(nextSource.streamUrl, nextSource.referer)
                            }, 2000)
                        }
                    }
                }
            }
        })
    }

    private fun observeStreams() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.streams.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.progressBar.isVisible = true
                                binding.tvBuffering.text = "Loading streams…"
                                binding.tvBuffering.isVisible = true
                            }
                            is Resource.Success -> {
                                binding.progressBar.isVisible = false
                                setupStreamSelector(resource.data)
                                resource.data.firstOrNull()?.let { source ->
                                    playStream(source.streamUrl, source.referer)
                                }
                            }
                            is Resource.Error -> {
                                binding.progressBar.isVisible = false
                                binding.tvError.isVisible = true
                                binding.tvError.text = resource.message
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupStreamSelector(streams: List<StreamSource>) {
        val adapter = StreamSelectorAdapter(streams) { source ->
            viewModel.selectStream(source)
            playStream(source.streamUrl, source.referer)
            binding.rvStreams.isVisible = false
        }
        binding.rvStreams.adapter = adapter
        binding.btnSources.setOnClickListener {
            binding.rvStreams.isVisible = !binding.rvStreams.isVisible
        }
        binding.btnSources.isVisible = streams.size > 1
    }

    private fun playStream(url: String, referer: String = "") {
        binding.tvError.isVisible = false
        val dataSourceFactory = OkHttpDataSource.Factory(
            okHttpClient.newBuilder()
                .apply { if (referer.isNotBlank()) addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().header("Referer", referer).build())
                }}
                .build()
        )
        val mediaItem = MediaItem.fromUri(url)
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        player?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    private fun setupControls() {
        binding.btnPip.setOnClickListener { enterPiP() }
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        enableImmersiveMode()
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }

    private fun enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isInPictureInPictureMode) player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
