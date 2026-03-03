package com.example.tutorial.com.example.tutorial.presentation.feature.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorial.com.example.tutorial.domain.model.SymptomCatalog
import com.example.tutorial.data.repository.SymptomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class SymptomUi(
    val id: String,
    val label: String,
    val picked: Boolean = false,
    val severity: Int = 0
)

@HiltViewModel
class SymptomViewModel @Inject constructor(
    private val repo: SymptomRepository
) : ViewModel() {

    private val _day       = MutableStateFlow(todayInt())
    val day: StateFlow<Int> = _day.asStateFlow()

    private val _symptoms  = MutableStateFlow(initUi())
    val symptoms: StateFlow<List<SymptomUi>> = _symptoms.asStateFlow()

    private val _markers   = MutableStateFlow(setOf<Int>())
    val markers: StateFlow<Set<Int>> = _markers.asStateFlow()

    init {
        viewModelScope.launch {
            repo.pullRemoteToLocal()
            loadFromDb(_day.value)
            refreshMonthMarkers()
        }
    }

    fun selectDay(newDay: Int) {
        if (newDay > todayInt() || newDay == _day.value) return
        _day.value = newDay
        viewModelScope.launch {
            loadFromDb(newDay)
            refreshMonthMarkers()
        }
    }

    fun selectMonth(year: Int, month: Int) {
        val firstDayInt = year * 10000 + month * 100 + 1
        val sameMonth   = (_day.value / 100) == (firstDayInt / 100)
        if (!sameMonth) {
            _day.value = firstDayInt
        }
        viewModelScope.launch {
            loadFromDb(_day.value)
            refreshMonthMarkers()
        }
    }

    fun toggle(symId: String) {
        if (_day.value > todayInt()) return
        _symptoms.update { list ->
            list.map {
                if (it.id != symId) it else {
                    val next = (it.severity + 1) % 4
                    it.copy(picked = next != 0, severity = next)
                }
            }
        }
    }

    fun save() = viewModelScope.launch {
        val map = _symptoms.value
            .filter { it.picked }
            .associate { it.id to it.severity }
        repo.insertBatch(epochMidnight(_day.value), map)
        loadFromDb(_day.value)
        refreshMonthMarkers()
        repo.pushUnsynced()
    }

    private suspend fun loadFromDb(dayInt: Int) {
        val rows = repo.getForDay(dayInt)
        _symptoms.value = initUi().map { ui ->
            val row = rows.firstOrNull { it.symptomId == ui.id }
            if (row != null) ui.copy(picked = true, severity = row.severity) else ui
        }
    }

    private suspend fun refreshMonthMarkers() {
        val monthStart = _day.value / 100 * 100 + 1
        val monthEnd   = monthStart + 99
        _markers.value = repo.daysWithSymptoms(monthStart, monthEnd).toSet()
    }

    private fun initUi() = SymptomCatalog.items.map { SymptomUi(it.id, it.label) }

    private fun epochMidnight(dayInt: Int): Long =
        LocalDate.of(dayInt / 10000, (dayInt / 100) % 100, dayInt % 100)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun todayInt(): Int =
        dayInt(Instant.now().toEpochMilli())

    private fun dayInt(millis: Long): Int =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }
}
