package com.adrian.zarza.turisty.placeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrian.zarza.turisty.database.PlaceDatabaseDAO

class PlaceDetailViewModelFactory(
        private val placeKey: Long,
        private val address: String,
        private val latlng: String,
        private val dataSource: PlaceDatabaseDAO) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceDetailViewModel::class.java)) {
            return PlaceDetailViewModel(placeKey, address, latlng, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}