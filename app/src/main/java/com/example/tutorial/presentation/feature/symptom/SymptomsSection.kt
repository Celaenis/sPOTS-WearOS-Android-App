package com.example.tutorial.com.example.tutorial.presentation.feature.symptom

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@SuppressLint("UnrememberedMutableState")
@Composable
fun SymptomsSection(vm: SymptomViewModel = hiltViewModel()) {

    LaunchedEffect(Unit) {
        val today = LocalDate.now()
        val todayInt = today.year * 10000 + today.monthValue * 100 + today.dayOfMonth
        vm.selectMonth(today.year, today.monthValue)
        vm.selectDay(todayInt)
    }

    var monthOffset by remember { mutableStateOf(0) }

    val monthAnchor by derivedStateOf {
        LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(monthOffset.toLong())
    }

    LaunchedEffect(monthAnchor) {
        val firstOfCurr = LocalDate.now().withDayOfMonth(1)
        if (monthAnchor.isAfter(firstOfCurr)) monthOffset--
    }

    LaunchedEffect(monthAnchor) {
        vm.selectMonth(monthAnchor.year, monthAnchor.monthValue)
    }

    val firstDow = monthAnchor.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        .replaceFirstChar { it.titlecase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton({ monthOffset-- }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                monthAnchor.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    .replaceFirstChar { it.titlecase() } + " ${monthAnchor.year}",
                style = MaterialTheme.typography.titleMedium
            )
        }
        IconButton(
            onClick = { monthOffset++ },
            enabled = !monthAnchor.isEqual(LocalDate.now().withDayOfMonth(1))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
        }
    }
    Spacer(Modifier.height(8.dp))

    val list by vm.symptoms.collectAsState()
    val dayInt by vm.day.collectAsState()
    val markers by vm.markers.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var showCal by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {

        if (showCal) {
            MonthCalendar(
                year = monthAnchor.year,
                month = monthAnchor.monthValue,
                markers = markers,
                onDayClick = { vm.selectDay(it); showCal = false }
            )
            Spacer(Modifier.height(12.dp))
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Symptoms ${prettyDay(dayInt)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { showCal = !showCal }) {
                        Text("Calendar")
                    }
                }

                FlowRow(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    list.filter { it.picked }.forEach {
                        SymptomTag(label = it.label, severity = it.severity, onClick = {})
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(onClick = { showSheet = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (list.any { it.picked }) "Edit" else "Add")
                }
            }
        }
    }

    if (showSheet) {
        SymptomPickerSheet(
            data = list,
            onToggle = vm::toggle,
            onSave = vm::save,
            onDismiss = { showSheet = false }
        )
    }
}

private fun prettyDay(d: Int): String = "%02d/%02d".format(d % 100, (d / 100) % 100)