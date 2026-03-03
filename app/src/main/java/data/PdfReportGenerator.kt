package com.example.tutorial.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.tutorial.com.example.tutorial.domain.model.SymptomCatalog
import com.example.tutorial.data.local.EpisodeEntity
import com.example.tutorial.data.local.SensorData
import com.example.tutorial.data.local.SymptomEntity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.roundToInt

object PdfReportGenerator {

    suspend fun generate(
        ctx: Context,
        patient: Triple<String, Int, String>,
        range: LongRange,
        hrSeries: List<SensorData>,
        episodes: List<EpisodeEntity>,
        symptoms: List<SymptomEntity>,
        dest: Uri,
        resolver: ContentResolver
    ) {
        val pdf = PdfDocument()

        buildPage1(pdf, ctx, patient, range, hrSeries, episodes)
        buildEpisodesPage(pdf, episodes)
        buildSymptomsPage(pdf, ctx, symptoms)

        resolver.openOutputStream(dest, "w")?.use { pdf.writeTo(it) }
            ?: error("Can't open $dest")
        pdf.close()
    }

    private suspend fun buildPage1(
        pdf: PdfDocument,
        ctx: Context,
        patient: Triple<String, Int, String>,
        range: LongRange,
        hrSeries: List<SensorData>,
        episodes: List<EpisodeEntity>
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val c = page.canvas

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            typeface = Typeface.DEFAULT_BOLD
            textSize = 20f
        }
        val (name, age, sex) = patient
        c.drawText("POTS Report – $name, $age y/o, $sex", 40f, 50f, titlePaint)

        val subPaint = Paint(titlePaint).apply {
            typeface = Typeface.DEFAULT
            textSize = 14f
        }
        val df = DateTimeFormatter.ofPattern("dd MMM yyyy")
        val startDate = Instant.ofEpochMilli(range.first)
            .atZone(ZoneId.systemDefault()).toLocalDate().format(df)
        val endDate = Instant.ofEpochMilli(range.last)
            .atZone(ZoneId.systemDefault()).toLocalDate().format(df)
        c.drawText("Measurement interval: $startDate → $endDate", 40f, 80f, subPaint)

        val maxDelta = episodes.maxOfOrNull { it.delta } ?: 0
        val rec = if (maxDelta >= 50 || episodes.any { it.peakHr >= 125 }) {
            "Elevated orthostatic tachycardia detected. Refer to autonomic specialist."
        } else {
            "Orthostatic HR increase within moderate range. Routine follow-up."
        }
        val recPaint = Paint(subPaint).apply {
            textSize = 13f
            color = if (rec.startsWith("Elevated")) Color.RED else Color.DKGRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        c.drawText(rec, 40f, 110f, recPaint)

        val figPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.DKGRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        val hrBmp = withContext(Dispatchers.Main) { buildAdaptiveHrChart(ctx, hrSeries, range) }
        val chartY = 150f
        c.drawBitmap(Bitmap.createScaledBitmap(hrBmp, 515, 260, true), 40f, chartY, null)

        run {
            val caption1 =
                "Figure 1. Continuous heart-rate (HR) profile over the measurement interval."
            val textW1 = figPaint.measureText(caption1)
            val x1 = 40f + (515f - textW1) / 2f
            c.drawText(caption1, x1, chartY + 260f + 12f, figPaint)
        }

        if (hrSeries.isNotEmpty()) {
            val vals = hrSeries.map { it.heartRate }
            val min = vals.minOrNull() ?: 0
            val max = vals.maxOrNull() ?: 0
            val avg = vals.average().roundToInt()

            val statsY = chartY + 260f + 40f
            val statPaint = Paint(subPaint).apply {
                textSize = 12f
                color = Color.BLACK
            }
            c.drawText(
                "Samples: ${hrSeries.size}    Min HR: $min bpm    Avg HR: $avg bpm    Max HR: $max bpm",
                40f, statsY, statPaint
            )
            Paint(statPaint).apply {
                textSize = 10f
                color = Color.DKGRAY
            }.let {
                c.drawText(
                    "(Normal supine HR 60–100 bpm; orthostatic delta HR >=30 bpm diagnostic)",
                    40f, statsY + 15f, it
                )
            }

            run {
                val caption2 = "Figure 2. Summary statistics compared to normal HR ranges."
                val textW2 = figPaint.measureText(caption2)
                val x2 = 40f + (515f - textW2) / 2f
                c.drawText(caption2, x2, statsY + 35f, figPaint)
            }
        }

        pdf.finishPage(page)
    }

    private fun buildAdaptiveHrChart(
        ctx: Context,
        raw: List<SensorData>,
        range: LongRange
    ): Bitmap {
        val days = ceil((range.last - range.first) / 86_400_000.0).toInt()
        val bucketMs = when {
            days < 3 -> 0L
            days < 31 -> 3_600_000L
            else -> 86_400_000L
        }

        val entries = if (bucketMs == 0L) {
            val step = (raw.size / 4000f).coerceAtLeast(1f).roundToInt()
            raw.indices.step(step).map { i ->
                Entry(raw[i].timestamp.toFloat(), raw[i].heartRate.toFloat())
            }
        } else {
            raw.groupBy { it.timestamp / bucketMs }
                .toSortedMap()
                .map { (b, list) ->
                    val sorted = list.map { it.heartRate }.sorted()
                    val m = sorted[list.size / 2]
                    Entry((b * bucketMs).toFloat(), m.toFloat())
                }
        }

        val chart = LineChart(ctx).apply {
            layout(0, 0, 900, 450)
            setBackgroundColor(Color.WHITE)
            description = Description().apply { text = "" }
            legend.isEnabled = false
            axisRight.isEnabled = false

            axisLeft.apply {
                textSize = 10f
                granularity = 10f
                setDrawGridLines(true)
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 8f
                setDrawGridLines(false)
                labelRotationAngle = -45f
                granularity = when {
                    days < 3 -> 4f * 3_600_000f
                    days < 31 -> 24f * 3_600_000f
                    else -> 7f * 24 * 3_600_000f
                }
                valueFormatter = object : ValueFormatter() {
                    private val fh = DateTimeFormatter.ofPattern("dd HH:mm")
                    private val fd = DateTimeFormatter.ofPattern("dd MMM")
                    override fun getFormattedValue(v: Float): String {
                        val dt = Instant.ofEpochMilli(v.toLong())
                            .atZone(ZoneId.systemDefault())
                        return if (days < 3) fh.format(dt) else fd.format(dt)
                    }
                }
            }
        }

        val ds = LineDataSet(entries, "HR").apply {
            setDrawCircles(false)
            lineWidth = 1.5f
            color = Color.parseColor("#1976D2")
        }
        chart.data = LineData(ds)
        chart.invalidate()

        return Bitmap.createBitmap(900, 450, Bitmap.Config.ARGB_8888).also {
            chart.draw(Canvas(it))
        }
    }

    private fun buildEpisodesPage(pdf: PdfDocument, episodes: List<EpisodeEntity>) {

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page = pdf.startPage(pageInfo)
        val c = page.canvas

        val titleP = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        c.drawText("Orthostatic Tachycardia Episodes", 40f, 40f, titleP)

        if (episodes.isEmpty()) {
            Paint(titleP).apply {
                textSize = 14f
                typeface = Typeface.DEFAULT
            }.let {
                c.drawText(
                    "No POTS-like episodes were recorded during this interval.",
                    40f, 80f, it
                )
            }
            pdf.finishPage(page)
            return
        }

        val bodyP = Paint(titleP).apply {
            textSize = 14f
            typeface = Typeface.DEFAULT
        }
        val total = episodes.size
        val avgDelta =
            episodes.map { it.delta }.average().let { if (it.isNaN()) 0.0 else it }.roundToInt()
        val maxDelta = episodes.maxOf { it.delta }
        val pk = episodes.maxOf { it.peakHr }

        c.drawText(
            "Total episodes: $total    Avg delta HR: $avgDelta bpm    Max deltaHR: $maxDelta bpm    Peak HR: $pk bpm",
            40f, 70f, bodyP
        )
        Paint(bodyP).apply {
            textSize = 12f
            color = Color.DKGRAY
        }.let {
            c.drawText("(Ref: delta HR >=30 bpm; Peak HR >120 bpm = severe)", 40f, 90f, it)
        }

        val hdrP = Paint(bodyP).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        listOf("Onset (Date/Time)", "delta HR (bpm)", "Peak HR (bpm)").forEachIndexed { i, h ->
            c.drawText(h, 40f + i * 200f, 115f, hdrP)
        }

        val df = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
        episodes.forEachIndexed { idx, ep ->
            val y = 140f + idx * 20f
            val date = Instant.ofEpochMilli(ep.startTimestamp)
                .atZone(ZoneId.systemDefault())
                .format(df)

            c.drawText(date, 40f, y, bodyP.apply { color = Color.BLACK })
            c.drawText("${ep.delta}", 240f, y, bodyP.apply {
                color = if (ep.delta >= 40) Color.RED else Color.BLACK
            })
            c.drawText("${ep.peakHr}", 440f, y, bodyP.apply {
                color = if (ep.peakHr > 120) Color.RED else Color.BLACK
            })
        }

        Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.DKGRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }.also { figP ->
            val caption = "Figure 3. Onset time, orthostatic delta HR and peak HR for each episode."
            val w = figP.measureText(caption)
            val x = 40f + (515f - w) / 2f
            val y = 140f + episodes.size * 20f + 25f
            c.drawText(caption, x, y, figP)
        }

        pdf.finishPage(page)
    }


    private suspend fun buildSymptomsPage(
        pdf: PdfDocument,
        ctx: Context,
        rows: List<SymptomEntity>
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 3).create()
        val page = pdf.startPage(pageInfo)
        val c = page.canvas

        val titleP = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText("Symptom Burden Analysis", 40f, 40f, titleP)

        val bySym = rows.groupBy { it.symptomId }
        val freq = bySym.mapValues { it.value.size }
        val byHour = IntArray(24).also { arr ->
            rows.forEach {
                val h = Instant.ofEpochMilli(it.millis)
                    .atZone(ZoneId.systemDefault()).hour
                arr[h]++
            }
        }

        val figP = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.DKGRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        val barBmp = withContext(Dispatchers.Main) { buildSymptomFreqChart(ctx, freq) }
        val barY = 60f
        c.drawBitmap(Bitmap.createScaledBitmap(barBmp, 515, 200, true), 40f, barY, null)
        run {
            val caption4 = "Figure 4. Frequency of the top 10 reported symptoms."
            val textW4 = figP.measureText(caption4)
            val x4 = 40f + (515f - textW4) / 2f
            c.drawText(caption4, x4, barY + 200f + 12f, figP)
        }

        val lineBmp = withContext(Dispatchers.Main) { buildSymptomHourlyChart(ctx, byHour) }
        val lineY = barY + 200f + 40f
        c.drawBitmap(Bitmap.createScaledBitmap(lineBmp, 515, 200, true), 40f, lineY, null)
        run {
            val caption5 = "Figure 5. Hourly distribution of symptom reports (0–24 h)."
            val textW5 = figP.measureText(caption5)
            val x5 = 40f + (515f - textW5) / 2f
            c.drawText(caption5, x5, lineY + 200f + 12f, figP)
        }

        val hdrP = Paint().apply {
            isAntiAlias = true
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        val tableY = lineY + 200f + 40f
        c.drawText("Symptom", 40f, tableY, hdrP)
        c.drawText("Count", 260f, tableY, hdrP)
        c.drawText("Avg Severity", 380f, tableY, hdrP)


        val bodyP = Paint(hdrP).apply { typeface = Typeface.DEFAULT }
        var y = tableY + 20f
        bySym.entries
            .sortedByDescending { it.value.size }
            .take(15)
            .forEach { (id, list) ->
                val lbl = SymptomCatalog.map[id]?.label ?: id
                val cnt = list.size
                val avg = list.map { it.severity }.average()
                c.drawText(lbl, 40f, y, bodyP)
                c.drawText("$cnt", 260f, y, bodyP)
                c.drawText("%.1f".format(avg), 380f, y, bodyP)
                y += 20f
            }

        pdf.finishPage(page)
    }


    private fun buildSymptomFreqChart(
        ctx: Context,
        freq: Map<String, Int>
    ): Bitmap {
        val top = freq.toList().sortedByDescending { it.second }.take(10)
        val labels = top.map { SymptomCatalog.map[it.first]?.label ?: it.first }
        val entries = top.mapIndexed { i, p -> BarEntry(i.toFloat(), p.second.toFloat()) }

        val ds = BarDataSet(entries, "").apply {
            valueTextSize = 9f
            color = Color.parseColor("#0288D1")
            setDrawValues(true)
        }

        val chart = BarChart(ctx).apply {
            layout(0, 0, 900, 400)
            setBackgroundColor(Color.WHITE)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.apply {
                granularity = 1f
                textSize = 9f
                setDrawGridLines(true)
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 8f
                labelRotationAngle = -45f
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
            }
            data = BarData(ds)
            setFitBars(true)
            invalidate()
        }

        return Bitmap.createBitmap(900, 400, Bitmap.Config.ARGB_8888).also {
            chart.draw(Canvas(it))
        }
    }

    private fun buildSymptomHourlyChart(
        ctx: Context,
        byHour: IntArray
    ): Bitmap {
        val entries = byHour.mapIndexed { h, v ->
            Entry(h.toFloat(), v.toFloat())
        }
        val ds = LineDataSet(entries, "").apply {
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 1.6f
            valueTextSize = 8f
            color = Color.parseColor("#00796B")
        }

        val chart = LineChart(ctx).apply {
            layout(0, 0, 900, 400)
            setBackgroundColor(Color.WHITE)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false

            axisLeft.apply {
                granularity = 1f
                textSize = 9f
                setDrawGridLines(true)
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 8f
                labelRotationAngle = -45f
                setDrawGridLines(false)

                granularity = 1f
                setLabelCount(8, true)

                valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        val hour = value.toInt().coerceIn(0, 23)
                        return "${hour}h"
                    }
                }

                axisMinimum = 0f
                axisMaximum = 23f
            }

            data = LineData(ds)
            invalidate()
        }

        return Bitmap.createBitmap(900, 400, Bitmap.Config.ARGB_8888).also {
            chart.draw(Canvas(it))
        }
    }
}
