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
class HomeViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _liveFootball = MutableStateFlow<Resource<List<Match>>>(Resource.Loading())
    val liveFootball: StateFlow<Resource<List<Match>>> = _liveFootball.asStateFlow()

    private val _todayMatches = MutableStateFlow<Resource<List<Match>>>(Resource.Loading())
    val todayMatches: StateFlow<Resource<List<Match>>> = _todayMatches.asStateFlow()

    private val _liveCricket = MutableStateFlow<Resource<List<CricketMatch>>>(Resource.Loading())
    val liveCricket: StateFlow<Resource<List<CricketMatch>>> = _liveCricket.asStateFlow()

    private val _selectedSport = MutableStateFlow(SportType.FOOTBALL)
    val selectedSport: StateFlow<SportType> = _selectedSport.asStateFlow()

    init {
        loadLiveMatches()
    }

    fun loadLiveMatches() {
        viewModelScope.launch {
            repository.getLiveFootballMatches()
                .collect { _liveFootball.value = it }
        }
        viewModelScope.launch {
            repository.getLiveCricketMatches()
                .collect { _liveCricket.value = it }
        }
        viewModelScope.launch {
            repository.getTodayFootballMatches()
                .collect { _todayMatches.value = it }
        }
    }

    fun selectSport(sport: SportType) {
        _selectedSport.value = sport
    }
}
