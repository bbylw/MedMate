package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CatalogDrug
import com.example.data.EvidenceDrugCatalog
import com.example.data.Medication
import com.example.data.MedicationRepository
import com.example.data.inferPeriodFromTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {

    val medications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPeriod = MutableStateFlow("全部")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
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
        repository.insertAll(EvidenceDrugCatalog.defaultSampleMedications())
    }

    fun insert(medication: Medication) = viewModelScope.launch {
        repository.insert(medication)
    }

    fun update(medication: Medication) = viewModelScope.launch {
        repository.update(medication)
    }

    fun toggleTaken(medication: Medication) = viewModelScope.launch {
        val nextTaken = !medication.isTakenToday
        val updatedStock = if (nextTaken) {
            max(0, medication.stockCount - 1)
        } else {
            medication.stockCount + 1
        }
        val updatedStreak = if (nextTaken) {
            medication.streakDays + 1
        } else {
            max(1, medication.streakDays - 1)
        }
        repository.update(
            medication.copy(
                isTakenToday = nextTaken,
                stockCount = updatedStock,
                streakDays = updatedStreak
            )
        )
    }

    fun markPeriodAllTaken(period: String) = viewModelScope.launch {
        val targets = medications.value.filter {
            (period == "全部" || it.period == period) && !it.isTakenToday
        }
        targets.forEach { med ->
            repository.update(
                med.copy(
                    isTakenToday = true,
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
        repository.resetDailyStatus()
    }

    fun addFromCatalog(
        drug: CatalogDrug,
        customDosage: String = drug.defaultDosage,
        customTime: String = drug.recommendedTime,
        customMeal: String = drug.mealInstruction
    ) = viewModelScope.launch {
        val finalDosage = customDosage.trim().ifBlank { drug.defaultDosage }
        val finalTime = customTime.trim().ifBlank { drug.recommendedTime }
        val existing = medications.value.firstOrNull { it.name == drug.name }
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
            stockCount = 28,
            streakDays = 1,
            color = drug.colorHex.toInt()
        )
        repository.insert(med)
    }

    fun updateDosage(medication: Medication, newDosage: String) = viewModelScope.launch {
        val cleanDosage = newDosage.trim()
        if (cleanDosage.isNotEmpty()) {
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
