package com.mehmetboluk.trackerapp.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetboluk.trackerapp.db.Run
import com.mehmetboluk.trackerapp.other.SortType
import com.mehmetboluk.trackerapp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel(){

    private val runSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runSortedByAvgSpeed = mainRepository.getAllRunsSortedByAverageSpeed()
    private val runSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runSortedByDate) { result ->
            if(sortType == SortType.DATE){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByTimeInMillis) { result ->
            if(sortType == SortType.DISTANCE_TIME){
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType){
        SortType.DATE -> runSortedByDate.value?.let { runs.value = it }
        SortType.DISTANCE_TIME -> runSortedByTimeInMillis.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runSortedByCaloriesBurned.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run : Run) = viewModelScope.launch {
            mainRepository.insertRun(run)
        }

}