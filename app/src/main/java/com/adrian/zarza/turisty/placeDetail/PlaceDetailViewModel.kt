package com.adrian.zarza.turisty.placeDetail

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adrian.zarza.turisty.database.Place
import com.adrian.zarza.turisty.database.PlaceDatabaseDAO
import kotlinx.coroutines.*

class PlaceDetailViewModel(
        private val placeKey: Long = 0L,
        dataSource: PlaceDatabaseDAO) : ViewModel() , Observable {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var place: LiveData<Place>

    var lastPlace = MutableLiveData<Place?>()

    private var titlePlace = MutableLiveData<String>()
    private var descriptionPlace = MutableLiveData<String>()
    private var addressPlace = MutableLiveData<String>()

    private val _navigateToPlaceFragment =  MutableLiveData<Boolean?>()

    val navigateToPlaceFragment: LiveData<Boolean?> = _navigateToPlaceFragment

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackBarEvent: LiveData<Boolean> = _showSnackbarEvent

    @Bindable var placeTitleWord : String? = titlePlace.value
        set(value) {
            if (field != value) {
                field = value
            }
        }

    @Bindable var placeDescriptionWord : String? = descriptionPlace.value
        set(value) {
            if (field != value) {
                field = value
            }
        }

    @Bindable var placeAddressWord : String? = addressPlace.value
        set(value) {
            if (field != value) {
                field = value
            }
        }

    @Bindable
    fun getTask() = place

    init {
        place = database.getPlaceWithId(placeKey)
        titlePlace.value = place.value?.titlePlace
        descriptionPlace.value = place.value?.descriptionPlace
        addressPlace.value = place.value?.addressPlace

        initializeLastPlace()
    }

    private fun initializeLastPlace() {
        uiScope.launch {
            lastPlace.value = getLastPlaceFromDB()
        }
    }

    private suspend fun getLastPlaceFromDB(): Place? {
        return withContext(Dispatchers.IO){
            var place = database.getLastPlace()
            place
        }
    }

    fun doneNavigating() {
        _navigateToPlaceFragment.value = null
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    fun onUpdatePlace() {
        if(placeTitleWord == null || placeDescriptionWord == null){
            _showSnackbarEvent.value = true
        } else {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    if (database.get(placeKey) == null) {
                        val lastPlace = lastPlace
                        lastPlace.value?.titlePlace = placeTitleWord.toString()
                        lastPlace.value?.descriptionPlace = placeDescriptionWord.toString()
                        database.update(lastPlace.value!!)

                    } else {
                        val lastPlace = database.get(placeKey) ?: return@withContext
                        lastPlace.titlePlace = placeTitleWord.toString()
                        lastPlace.descriptionPlace = placeDescriptionWord.toString()
                        database.update(lastPlace)
                    }
                }
                _navigateToPlaceFragment.value = true
            }
        }
    }

    fun onClearItem() {
        uiScope.launch {
            clearPlaceWithId()
            lastPlace.value = null
        }
        //_showSnackbarEvent.value = true
        _navigateToPlaceFragment.value = true
    }

    private suspend fun clearPlaceWithId() {
        withContext(Dispatchers.IO) {
            database.deleteWithId(placeKey)
        }
    }

    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback) {
        //callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback) {
        //callbacks.remove(callback)
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }
}