package com.example.tutorial.com.example.tutorial.presentation.feature.report

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorial.data.repository.EpisodeRepository
import com.example.tutorial.data.repository.SensorDataRepository
import com.example.tutorial.data.repository.SymptomRepository
import com.example.tutorial.data.PdfReportGenerator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sensorRepo: SensorDataRepository,
    private val episodeRepo: EpisodeRepository,
    private val symptomRepo: SymptomRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val ctx: Context
) : ViewModel() {


    fun buildPdf(
        patient: Triple<String, Int, String>,
        from: LocalDate,
        to: LocalDate,
        dest: Uri,
        resolver: ContentResolver,
        onReady: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {

        try {
            sensorRepo.pullRemoteToLocal(auth, firestore)
            val startMs = from.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            val endMs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - 1

            val hrSeries = sensorRepo.range(startMs, endMs)
            val episodes = episodeRepo.range(startMs, endMs)
            val symptoms = symptomRepo.range(startMs, endMs)

            PdfReportGenerator.generate(
                ctx,
                patient,
                startMs..endMs,
                hrSeries,
                episodes,
                symptoms,
                dest,
                resolver
            )

            withContext(Dispatchers.Main) { onReady(dest) }

        } catch (t: Throwable) {
            withContext(Dispatchers.Main) { onError(t) }
        }
    }
}