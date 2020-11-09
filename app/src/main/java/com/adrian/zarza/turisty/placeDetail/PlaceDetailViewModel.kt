package com.adrian.zarza.turisty.placeDetail

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adrian.zarza.turisty.database.Place
import com.adrian.zarza.turisty.database.PlaceDatabaseDAO
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*

class PlaceDetailViewModel(
        private val placeKey: Long = 0L,
        private val address: String,
        private val latLng: String,
        dataSource: PlaceDatabaseDAO) : ViewModel() , Observable {

    val database = dataSource

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var place: LiveData<Place>

    var lastPlace = MutableLiveData<Place?>()

    private var _titlePlace = MutableLiveData<String>()
    private var _descriptionPlace = MutableLiveData<String>()
    private var _addressPlace = MutableLiveData<String>()

    private val _navigateToPlaceFragment =  MutableLiveData<Boolean>()

    val navigateToPlaceFragment: LiveData<Boolean> = _navigateToPlaceFragment

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackBarEvent: LiveData<Boolean> = _showSnackbarEvent

    init {
        place = database.getPlaceWithId(placeKey)
        _titlePlace.value = place.value?.titlePlace
        _descriptionPlace.value = place.value?.descriptionPlace
        _addressPlace.value = address

        initializeLastPlace()
    }

    @Bindable var placeTitleWord : String? = _titlePlace.value
        set(value) {
            if (field != value) {
                field = value
            }
        }

    @Bindable var placeDescriptionWord : String? = _descriptionPlace.value
        set(value) {
            if (field != value) {
                field = value
            }
        }

    @Bindable var placeAddressWord : String? = _addressPlace.value
        set(value) {
            if (field != value) {
                field = value
            }
        }

    @Bindable
    fun getPlace() = place

    private fun isNewInsertion(place : LiveData<Place>): Boolean{
        return place.value == null
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
            if(!isNewInsertion(place)) {
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
            } else {
                onNewInsertion()
            }
        }
    }

    private fun onNewInsertion(){
        uiScope.launch {
            val newPlace = Place(placeTitleWord!!, placeDescriptionWord!!, "LatLong",address)
            insert(newPlace)
            lastPlace.value = getLastPlaceFromDB()
            //onTaskClicked(newTask.taskId)
        }
        _navigateToPlaceFragment.value = true
    }


    private suspend fun insert(place: Place){
        withContext(Dispatchers.IO){
            database.insert(place)
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