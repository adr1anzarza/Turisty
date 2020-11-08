package com.adrian.zarza.turisty.place

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrian.zarza.turisty.database.Place
import com.adrian.zarza.turisty.database.PlaceDatabaseDAO
import kotlinx.coroutines.*

class PlaceViewModel(val database: PlaceDatabaseDAO,
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

    private val _navigateToMapsFragment = MutableLiveData<Boolean>()
    val navigateToMapsFragment= _navigateToMapsFragment

    private val _navigateToMapsFragmentWithPlaceId = MutableLiveData<Long>()
    val navigateToMapsFragmentWithPlaceId= _navigateToMapsFragment


    init {
        initializeLastPlace()
    }

    fun onTaskClicked(placeId: Long){
        _navigateToMapsFragmentWithPlaceId.value = placeId
    }

    fun onMapsFragmentNavigated() {
        _navigateToMapsFragment.value = null
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

    fun onStartTask() {
        uiScope.launch {
            //val newTask = Place("", "")
            //insert(newTask)
            lastPlace.value = getLastPlaceFromDB()
            //onTaskClicked(newTask.taskId)
        }

    }

    fun onNewPlace(){
        navigateToMapsFragment.value = true
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