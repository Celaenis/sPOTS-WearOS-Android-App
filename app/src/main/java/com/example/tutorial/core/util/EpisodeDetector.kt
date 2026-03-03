package com.example.tutorial.com.example.tutorial.core.util

import android.util.Log
import com.example.tutorial.com.example.tutorial.core.service.HeartRateData
import com.example.tutorial.com.example.tutorial.core.service.HeartRateMonitor
import com.example.tutorial.data.local.EpisodeEntity
import com.example.tutorial.data.repository.EpisodeRepository
import com.example.tutorial.data.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class EpisodeDetector @Inject constructor(
    hrMon: HeartRateMonitor,
    private val repo: EpisodeRepository,
    private val alert: EpisodeAlertHelper,
    profileRepo: ProfileRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var age = 30
    @Volatile
    private var sex = "Other"

    private val MIN_BASELINE_SAMPLES = 1
    private val deltaAdult = 30
    private val deltaTeen = 40
    private val minPeakHr = 110

    private var baseline = 0.0
    private var baselineSamples = 0
    private var standing = false
    private var startTs = 0L
    private var peakHr = 0

    private fun ema(old: Double, new: Int) = 0.3 * new + 0.7 * old

    init {
        scope.launch {
            profileRepo.profileFlow().collect { p ->
                p?.let { age = it.age; sex = it.sex }
            }
        }
        scope.launch {
            hrMon.receive()
                .filter { it.heartRate > 30 }
                .collect { onPacket(it) }
        }
    }

    private suspend fun onPacket(pkt: HeartRateData) {
        val ts = System.currentTimeMillis()
        if (pkt.isStanding && !standing) {
            standing = true
            startTs = ts
            peakHr = pkt.heartRate
        } else if (!pkt.isStanding && standing) {
            maybeSaveEpisode(ts)
            standing = false
        }
        if (standing) {
            if (pkt.heartRate > peakHr) peakHr = pkt.heartRate
            if (ts - startTs >= 600_000) {
                maybeSaveEpisode(ts)
                standing = false
            }
        }
        if (!standing) {
            baseline = if (baselineSamples == 0)
                pkt.heartRate.toDouble()
            else
                ema(baseline, pkt.heartRate)
            baselineSamples++
        }
    }

    private suspend fun maybeSaveEpisode(endTs: Long) {
        if (baselineSamples < MIN_BASELINE_SAMPLES) return
        val baseHr = baseline.roundToInt()
        val delta = peakHr - baseHr
        val thresh = if (age < 19) deltaTeen else deltaAdult - if (sex == "Female") 5 else 0
        if (delta >= thresh && peakHr >= minPeakHr) {
            val ep = EpisodeEntity(
                startTimestamp = startTs,
                endTimestamp = endTs,
                baselineHr = baseHr,
                peakHr = peakHr,
                delta = delta
            )
            repo.save(ep)
            alert.fire(ep)
            Log.i("EpisodeDetector", "Episode delta$delta, peak $peakHr")
        }
        baseline = 0.0
        baselineSamples = 0
    }
}
