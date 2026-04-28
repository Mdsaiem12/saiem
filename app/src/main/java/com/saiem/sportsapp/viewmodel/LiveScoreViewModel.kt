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
class LiveScoreViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<Resource<List<Match>>>(Resource.Loading())
    val matches: StateFlow<Resource<List<Match>>> = _matches.asStateFlow()

    private val _standings = MutableStateFlow<Resource<StandingTable>>(Resource.Loading())
    val standings: StateFlow<Resource<StandingTable>> = _standings.asStateFlow()

    private val _selectedCompetition = MutableStateFlow("PL")
    val selectedCompetition: StateFlow<String> = _selectedCompetition.asStateFlow()

    val competitions = listOf(
        Competition("PL", "Premier League", "🏴󠁧󠁢󠁥󠁮󠁧󠁿"),
        Competition("CL", "Champions League", "🇪🇺"),
        Competition("PD", "La Liga", "🇪🇸"),
        Competition("SA", "Serie A", "🇮🇹"),
        Competition("BL1", "Bundesliga", "🇩🇪"),
        Competition("FL1", "Ligue 1", "🇫🇷")
    )

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            repository.getLiveFootballMatches().collect { _matches.value = it }
        }
    }

    fun loadStandings(competitionCode: String) {
        _selectedCompetition.value = competitionCode
        viewModelScope.launch {
            repository.getStandings(competitionCode).collect { _standings.value = it }
        }
    }

    fun refresh() = loadMatches()
}

data class Competition(
    val code: String,
    val name: String,
    val flag: String
)
