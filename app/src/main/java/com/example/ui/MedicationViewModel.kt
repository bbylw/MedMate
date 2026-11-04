package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CatalogDrug
import com.example.data.EvidenceDrugCatalog
import com.example.data.Medication
import com.example.data.MedicationRepository
import com.example.data.inferPeriodFromTime
import com.example.data.normalizeTimeFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {

    private val _currentDateIso = MutableStateFlow(LocalDate.now().toString())
    val currentDateIso: StateFlow<String> = _currentDateIso.asStateFlow()

    val medications: StateFlow<List<Medication>> = combine(
        repository.allMedications,
        _currentDateIso
    ) { list, todayIso ->
        list.map { med ->
            if (med.isTakenToday && med.lastTakenDate != todayIso) {
                med.copy(isTakenToday = false, lastTakenTime = "")
            } else {
                med
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedPeriod = MutableStateFlow("全部")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
    }

    /**
     * Automatically synchronizes daily medication check-in status whenever the app starts,
     * resumes from background, receives a system date/time broadcast, or crosses 00:00 midnight.
     */
    fun syncDailyRefresh(
        todayIso: String = LocalDate.now().toString(),
        lastSyncedDate: String?,
        onRefreshed: (todayIso: String, refreshedCount: Int) -> Unit = { _, _ -> }
    ) {
        if (_currentDateIso.value != todayIso) {
            _currentDateIso.value = todayIso
        }
        viewModelScope.launch {
            val allMeds = repository.getAllOnce()
            var refreshedCount = 0
            val isNewDayRollover = !lastSyncedDate.isNullOrBlank() && lastSyncedDate != todayIso

            allMeds.forEach { med ->
                val isStaleTaken = med.isTakenToday && med.lastTakenDate != todayIso
                val missedYesterday = isNewDayRollover && med.lastTakenDate != lastSyncedDate && med.lastTakenDate != todayIso

                if (isStaleTaken || missedYesterday) {
                    if (isStaleTaken) refreshedCount++
                    val updatedStreak = if (missedYesterday && !isStaleTaken) {
                        0
                    } else {
                        med.streakDays
                    }
                    repository.update(
                        med.copy(
                            isTakenToday = if (med.lastTakenDate == todayIso) med.isTakenToday else false,
                            lastTakenTime = if (med.lastTakenDate == todayIso) med.lastTakenTime else "",
                            streakDays = updatedStreak
                        )
                    )
                }
            }
            onRefreshed(todayIso, refreshedCount)
        }
    }

    fun seedDefaultIfNeeded(alreadySeeded: Boolean, onMarkSeeded: () -> Unit) {
        if (alreadySeeded) return
        viewModelScope.launch {
            if (repository.getCount() == 0) {
                repository.insertAll(EvidenceDrugCatalog.defaultSampleMedications())
            }
            onMarkSeeded()
        }
    }

    fun loadPresetRegimen() = viewModelScope.launch {
        val existingNames = repository.getAllOnce().map { it.name }.toSet()
        val toInsert = EvidenceDrugCatalog.defaultSampleMedications().filter { it.name !in existingNames }
        if (toInsert.isNotEmpty()) {
            repository.insertAll(toInsert)
        }
    }

    fun insert(medication: Medication) = viewModelScope.launch {
        val normalizedTime = normalizeTimeFormat(medication.time)
        val cleanMed = medication.copy(
            name = medication.name.trim(),
            time = normalizedTime,
            period = inferPeriodFromTime(normalizedTime),
            stockCount = max(0, medication.stockCount)
        )
        if (cleanMed.id == 0) {
            val existing = repository.getAllOnce().firstOrNull { it.name == cleanMed.name }
            if (existing != null) {
                repository.update(
                    cleanMed.copy(
                        id = existing.id,
                        isTakenToday = existing.isTakenToday,
                        lastTakenDate = existing.lastTakenDate,
                        lastTakenTime = existing.lastTakenTime,
                        streakDays = existing.streakDays
                    )
                )
                return@launch
            }
        }
        repository.insert(cleanMed)
    }

    fun restoreDeleted(medication: Medication) = viewModelScope.launch {
        repository.insert(medication.copy(id = 0))
    }

    fun update(medication: Medication) = viewModelScope.launch {
        val normalizedTime = normalizeTimeFormat(medication.time)
        repository.update(
            medication.copy(
                name = medication.name.trim(),
                time = normalizedTime,
                period = inferPeriodFromTime(normalizedTime),
                stockCount = max(0, medication.stockCount)
            )
        )
    }

    fun toggleTaken(medication: Medication) = viewModelScope.launch {
        val todayIso = LocalDate.now().toString()
        val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        _currentDateIso.value = todayIso

        val currentlyTakenToday = medication.isTakenToday && medication.lastTakenDate == todayIso
        val nextTaken = !currentlyTakenToday
        val updatedStock = if (nextTaken) {
            max(0, medication.stockCount - 1)
        } else {
            medication.stockCount + 1
        }
        val updatedStreak = if (nextTaken) {
            medication.streakDays + 1
        } else {
            max(0, medication.streakDays - 1)
        }
        repository.update(
            medication.copy(
                isTakenToday = nextTaken,
                lastTakenDate = if (nextTaken) todayIso else "",
                lastTakenTime = if (nextTaken) nowTime else "",
                stockCount = updatedStock,
                streakDays = updatedStreak
            )
        )
    }

    fun markPeriodAllTaken(period: String) = viewModelScope.launch {
        val todayIso = LocalDate.now().toString()
        val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        _currentDateIso.value = todayIso

        val periodTargets = medications.value.filter {
            (period == "全部" || it.period == period) && !it.isTakenToday
        }
        val targets = if (periodTargets.isNotEmpty()) {
            periodTargets
        } else {
            medications.value.filter { !it.isTakenToday }
        }
        targets.forEach { med ->
            repository.update(
                med.copy(
                    isTakenToday = true,
                    lastTakenDate = todayIso,
                    lastTakenTime = nowTime,
                    stockCount = max(0, med.stockCount - 1),
                    streakDays = med.streakDays + 1
                )
            )
        }
    }

    fun restock(medication: Medication, addAmount: Int) = viewModelScope.launch {
        repository.update(
            medication.copy(stockCount = max(0, medication.stockCount + addAmount))
        )
    }

    fun resetDailyStatus() = viewModelScope.launch {
        val todayIso = LocalDate.now().toString()
        _currentDateIso.value = todayIso
        val allMeds = repository.getAllOnce()
        allMeds.forEach { med ->
            if (med.isTakenToday) {
                val restoreTodayStock = med.lastTakenDate == todayIso
                repository.update(
                    med.copy(
                        isTakenToday = false,
                        lastTakenDate = "",
                        lastTakenTime = "",
                        stockCount = if (restoreTodayStock) med.stockCount + 1 else med.stockCount,
                        streakDays = if (restoreTodayStock) max(0, med.streakDays - 1) else med.streakDays
                    )
                )
            }
        }
    }

    fun addFromCatalog(
        drug: CatalogDrug,
        customDosage: String = drug.defaultDosage,
        customTime: String = drug.recommendedTime,
        customMeal: String = drug.mealInstruction
    ) = viewModelScope.launch {
        val finalDosage = customDosage.trim().ifBlank { drug.defaultDosage }
        val finalTime = normalizeTimeFormat(customTime.trim().ifBlank { drug.recommendedTime })
        val existing = repository.getAllOnce().firstOrNull { it.name == drug.name }
        if (existing != null) {
            repository.update(
                existing.copy(
                    dosage = finalDosage,
                    time = finalTime,
                    period = inferPeriodFromTime(finalTime),
                    mealInstruction = customMeal
                )
            )
            return@launch
        }
        val med = Medication(
            name = drug.name,
            genericName = drug.genericName,
            dosage = finalDosage,
            form = drug.form,
            time = finalTime,
            period = inferPeriodFromTime(finalTime),
            mealInstruction = customMeal,
            frequency = "每天",
            evidenceLevel = drug.evidenceLevel,
            clinicalNote = "${drug.mechanism}\n【安全警示】${drug.safetyNote}",
            isTakenToday = false,
            lastTakenDate = "",
            lastTakenTime = "",
            stockCount = 28,
            streakDays = 1,
            color = drug.colorHex.toInt()
        )
        repository.insert(med)
    }

    fun updateDosage(medication: Medication, newDosage: String) = viewModelScope.launch {
        val cleanDosage = newDosage.trim()
        if (cleanDosage.isNotEmpty() && cleanDosage != medication.dosage) {
            repository.update(medication.copy(dosage = cleanDosage))
        }
    }

    fun removeFromCatalog(drug: CatalogDrug) = viewModelScope.launch {
        repository.deleteByName(drug.name)
    }

    fun delete(medication: Medication) = viewModelScope.launch {
        repository.delete(medication)
    }

    fun clearAllMedications() = viewModelScope.launch {
        repository.deleteAll()
    }
}

class MedicationViewModelFactory(private val repository: MedicationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
