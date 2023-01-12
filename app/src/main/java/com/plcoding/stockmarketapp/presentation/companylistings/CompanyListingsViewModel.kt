package com.plcoding.stockmarketapp.presentation.companylistings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {
    var state by mutableStateOf(CompanyListingsState())
    private var searchJob: Job? = null

    fun onEvent(event: CompanyListingsEvent) {
        when (event) {
            is CompanyListingsEvent.Refresh -> {
                getCompanyListings(fetchFromRemote = true)
            }
            is CompanyListingsEvent.OnSearchQueryChange -> {
                state = state.copy(searchQuery = event.query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(DELAY_TIME_FOR_SEARCH_JOB.milliseconds)
                    getCompanyListings()
                }
            }
        }
    }

    fun getCompanyListings(
        query: String = state.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            repository
                .getCompanyListings(fetchFromRemote, query)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { companyListings ->
                                state = state.copy(companies = companyListings)
                            }
                        }
                        is Resource.Error -> Unit // TODO error handle
                        is Resource.Loading -> {
                            state = state.copy(isLoading = result.isLoading)
                        }
                    }
                }
        }
    }

    companion object {
        val DELAY_TIME_FOR_SEARCH_JOB = 500
    }
}
