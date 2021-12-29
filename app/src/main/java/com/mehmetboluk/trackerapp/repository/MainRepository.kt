package com.mehmetboluk.trackerapp.repository

import com.mehmetboluk.trackerapp.db.Run
import com.mehmetboluk.trackerapp.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao : RunDAO
) {

    suspend fun insertRun(run : Run) = runDao.insertRun(run)

    suspend fun deleteRun(run : Run) = runDao.delete(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunSortedByDate()

    fun getAllRunsSortedByAverageSpeed() = runDao.getAllRunSortedByAverageSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunSortedByCaloriesBurned()

    fun getAllRunsSortedByDistance() = runDao.getAllRunSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunSortedByTimeInMillis()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    fun getTotalAverageSpeed() = runDao.getTotalAverageSpeed()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDistance() = runDao.getTotalDistance()

}