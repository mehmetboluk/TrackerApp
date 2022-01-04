package com.mehmetboluk.trackerapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.mehmetboluk.trackerapp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel(){

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAverageSpeed()
    val runSortedByDate = mainRepository.getAllRunsSortedByDate()


}