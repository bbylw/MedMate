package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY time ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<Medication>)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM medications WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("DELETE FROM medications")
    suspend fun deleteAll()

    @Query("UPDATE medications SET isTakenToday = 0")
    suspend fun resetDailyStatus()

    @Query("UPDATE medications SET isTakenToday = 1 WHERE period = :period")
    suspend fun markPeriodTaken(period: String)
}
