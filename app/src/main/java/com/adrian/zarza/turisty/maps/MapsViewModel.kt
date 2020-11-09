package com.adrian.zarza.turisty.maps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrian.zarza.turisty.database.Place
import com.adrian.zarza.turisty.database.PlaceDatabaseDAO
import kotlinx.coroutines.*

class MapsViewModel(val database: PlaceDatabaseDAO,
                    application: Application) : AndroidViewModel(application){

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var lastPlace = MutableLiveData<Place?>()

    private var _places = database.getAllPlaces()

    val places : LiveData<List<Place>> = _places

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackBarEvent: LiveData<Boolean> = _showSnackbarEvent

    private val _navigateToPlaceDetailFragment = MutableLiveData<Boolean>()
    val navigateToPlaceDetailFragment= _navigateToPlaceDetailFragment

    private val _navigateToPlaceDetailFragmentWithPlaceId = MutableLiveData<Long>()
    val navigateToPlaceDetailFragmentWithPlaceId= _navigateToPlaceDetailFragment


    init {
        initializeLastPlace()
    }

    fun onTaskClicked(placeId: Long){
        _navigateToPlaceDetailFragmentWithPlaceId.value = placeId
    }

    fun onMapsFragmentNavigated() {
        _navigateToPlaceDetailFragment.value = null
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
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

    fun onPlaceSelected(){
        navigateToPlaceDetailFragment.value = true
    }

    fun onStartTask() {
        uiScope.launch {
            //val newTask = Place("", "")
            //insert(newTask)
            lastPlace.value = getLastPlaceFromDB()
            //onTaskClicked(newTask.taskId)
        }

    }

    fun onNewPlace(){
        navigateToPlaceDetailFragment.value = true
    }

    suspend fun insert(night: Place){
        withContext(Dispatchers.IO){
            database.insert(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            lastPlace.value = null
        }
        _showSnackbarEvent.value = true
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }
}