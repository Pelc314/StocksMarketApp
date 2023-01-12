package com.plcoding.stockmarketapp.presentation.companylistings // ktlint-disable package-name

sealed class CompanyListingsEvent {
    object Refresh : CompanyListingsEvent()
    data class OnSearchQueryChange(val query: String) : CompanyListingsEvent()
}
