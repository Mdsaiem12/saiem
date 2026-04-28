package com.saiem.sportsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saiem.sportsapp.data.model.*
import com.saiem.sportsapp.data.repository.SportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _streams = MutableStateFlow<Resource<List<StreamSource>>>(Resource.Loading())
    val streams: StateFlow<Resource<List<StreamSource>>> = _streams.asStateFlow()

    private val _selectedStream = MutableStateFlow<StreamSource?>(null)
    val selectedStream: StateFlow<StreamSource?> = _selectedStream.asStateFlow()

    private val _channels = MutableStateFlow<Resource<List<TvChannel>>>(Resource.Loading())
    val channels: StateFlow<Resource<List<TvChannel>>> = _channels.asStateFlow()

    fun loadStreams(matchId: String, matchKeyword: String) {
        viewModelScope.launch {
            _streams.value = Resource.Loading()
            try {
                val sources = repository.getStreamsForMatch(matchId, matchKeyword)
                _streams.value = Resource.Success(sources)
                if (sources.isNotEmpty()) _selectedStream.value = sources.first()
            } catch (e: Exception) {
                _streams.value = Resource.Error(e.message ?: "Failed to load streams")
            }
        }
    }

    fun loadChannels() {
        viewModelScope.launch {
            repository.getTvChannels().collect { _channels.value = it }
        }
    }

    fun selectStream(stream: StreamSource) {
        _selectedStream.value = stream
    }
}
