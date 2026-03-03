package com.example.tutorial.com.example.wear.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.acos
import kotlin.math.sqrt

@Singleton
class MotionDetector @Inject constructor(
    @ApplicationContext ctx: Context
) : SensorEventListener {

    private val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _isUpright = MutableStateFlow(false)
    val isStandingFlow: StateFlow<Boolean> = _isUpright

    private val rotM = FloatArray(9)
    private var tiltEma = 0f

    private val g = FloatArray(3)
    private val linWin = FloatArray(50)
    private var linPtr = 0
    private var linCnt = 0

    private val stepTs = ArrayDeque<Long>()

    private val VERT_MIN = 60f
    private val VERT_MAX = 120f
    private val WALK_ACCEL_VAR = 0.8f
    private val WALK_STEP_WIN = 1_500L

    private enum class S { SITTING, STANDING_STILL, WALKING }

    private var state = S.SITTING
    private var candState: S? = null
    private var candSince = 0L
    private val DEBOUNCE_MS = 800L

    fun start() {
        rotSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        accSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        stepSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stop() = sm.unregisterListener(this)

    override fun onSensorChanged(ev: SensorEvent) {
        when (ev.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotM, ev.values)
                val zWz = rotM[8].coerceIn(-1f, 1f)
                val tilt = Math.toDegrees(acos(zWz.toDouble())).toFloat()
                tiltEma = 0.1f * tilt + 0.9f * tiltEma
            }

            Sensor.TYPE_ACCELEROMETER -> {
                for (i in 0..2) g[i] = 0.8f * g[i] + 0.2f * ev.values[i]
                val lx = ev.values[0] - g[0]
                val ly = ev.values[1] - g[1]
                val lz = ev.values[2] - g[2]
                val lin = sqrt(lx * lx + ly * ly + lz * lz)
                linWin[linPtr] = lin
                linPtr = (linPtr + 1) % linWin.size
                if (linCnt < linWin.size) linCnt++
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                stepTs.addLast(SystemClock.elapsedRealtime())
            }
        }
        updateFsm()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun updateFsm() {
        val now = SystemClock.elapsedRealtime()
        while (stepTs.isNotEmpty() && now - stepTs.first() > WALK_STEP_WIN) {
            stepTs.removeFirst()
        }

        val window = linWin.take(linCnt)
        val mean = window.average().toFloat()
        val linVar = if (linCnt > 1) {
            val sum = window.fold(0f) { acc, v -> acc + (v - mean).let { it * it } }
            sum / (linCnt - 1)
        } else 0f

        val walkingDetected = (linVar > WALK_ACCEL_VAR) || stepTs.size >= 1

        val tiltAngle = tiltEma
        val nearVertical = tiltAngle in VERT_MIN..VERT_MAX

        when (state) {
            S.SITTING -> {
                if (nearVertical) {
                    candidate(S.STANDING_STILL, now)
                }
            }

            S.STANDING_STILL -> {
                if (walkingDetected) {
                    set(S.WALKING)
                } else if (!nearVertical) {
                    candidate(S.SITTING, now)
                }
            }

            S.WALKING -> {
                if (!walkingDetected) {
                    if (nearVertical) set(S.STANDING_STILL)
                    else set(S.SITTING)
                }
            }
        }
    }

    private fun candidate(target: S, now: Long) {
        if (candState != target) {
            candState = target
            candSince = now
            return
        }
        if (now - candSince > DEBOUNCE_MS) {
            set(target)
            candState = null
        }
    }

    private fun set(newState: S) {
        if (newState == state) return
        state = newState
        _isUpright.value = (state != S.SITTING)
    }
}
