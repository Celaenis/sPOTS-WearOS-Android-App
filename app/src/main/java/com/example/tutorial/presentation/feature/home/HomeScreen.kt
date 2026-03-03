package com.example.tutorial.com.example.tutorial.presentation.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tutorial.com.example.tutorial.presentation.feature.episode.EpisodeViewModel
import com.example.tutorial.data.local.EpisodeEntity
import com.example.tutorial.com.example.tutorial.presentation.feature.symptom.SymptomsSection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val epFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

private fun formatStart(ts: Long): String =
    Instant.ofEpochMilli(ts)
        .atZone(ZoneId.systemDefault())
        .format(epFormatter)


@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(),
    epiVm: EpisodeViewModel = hiltViewModel()
) {
    val health by viewModel.health.collectAsState()
    val recentData by viewModel.recentHeartRatesLiveData.observeAsState(emptyList())
    val episodes by epiVm.episodes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecentHeartRates()
        epiVm.refresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(Modifier.height(24.dp)) }
        item {
            Text(
                text = "Current Status",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
        }


        item {
            Text(
                text = "Heart-rate: ${health.heartRate} bpm",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (health.isStanding) "Standing" else "Sitting",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(24.dp))
        }
        item { Spacer(Modifier.height(24.dp)) }

        if (episodes.isNotEmpty()) {
            item {
                Text(
                    "Recent POTS-like episodes",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
            }
            items(episodes.take(10)) { ep: EpisodeEntity ->
                EpisodeCard(ep)
            }
        }


        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.syncAllInOne()
                    epiVm.refresh()
                }
            ) { Text("Sync and refresh") }

            Spacer(Modifier.height(32.dp))
        }

        item { SymptomsSection(); Spacer(Modifier.height(96.dp)) }
    }

}


@Composable
private fun EpisodeCard(ep: EpisodeEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Heart rate increased by  +${ep.delta} bpm " + "(${ep.baselineHr} -> ${ep.peakHr})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatStart(ep.startTimestamp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun epochToLocal(ms: Long): String =
    Instant.ofEpochMilli(ms)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .toString()