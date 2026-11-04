package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val medicationDao: MedicationDao) {
    val allMedications: Flow<List<Medication>> = medicationDao.getAllMedications()

    suspend fun getCount(): Int = medicationDao.getCount()
    suspend fun insert(medication: Medication) = medicationDao.insertMedication(medication)
    suspend fun insertAll(medications: List<Medication>) = medicationDao.insertAll(medications)
    suspend fun update(medication: Medication) = medicationDao.updateMedication(medication)
    suspend fun delete(medication: Medication) {
        medicationDao.deleteById(medication.id)
        medicationDao.deleteMedication(medication)
    }
    suspend fun deleteByName(name: String) = medicationDao.deleteByName(name)
    suspend fun deleteAll() = medicationDao.deleteAll()
    suspend fun getAllOnce(): List<Medication> = medicationDao.getAllMedicationsOnce()
    suspend fun resetDailyStatus() = medicationDao.resetDailyStatus()
    suspend fun refreshStaleDailyStatus(todayIso: String): Int = medicationDao.refreshStaleDailyStatus(todayIso)
    suspend fun markPeriodTaken(period: String, todayIso: String, nowTime: String) =
        medicationDao.markPeriodTaken(period, todayIso, nowTime)
}
