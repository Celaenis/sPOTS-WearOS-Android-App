package com.example.tutorial.com.example.tutorial.presentation.feature.symptom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

@Composable
fun MonthCalendar(
    year: Int,
    month: Int,
    markers: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    val first = java.time.LocalDate.of(year, month, 1)
    val firstDow = first.dayOfWeek.value % 7
    val days = first.lengthOfMonth()
    val rows = ceil((firstDow + days) / 7f).toInt()

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, modifier = Modifier.weight(1f), color = Color.Gray)
            }
        }
        var d = 1
        repeat(rows) {
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    if ((it == 0 && col < firstDow) || d > days) {
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f))
                    } else {
                        val dayInt = year * 10000 + month * 100 + d
                        val mark = markers.contains(dayInt)
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .clickable { onDayClick(dayInt) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(d.toString())
                                if (mark) Box(
                                    Modifier
                                        .size(6.dp)
                                        .background(
                                            Color(0xFFDE3BC1),
                                            MaterialTheme.shapes.extraSmall
                                        )
                                )
                            }
                        }
                        d++
                    }
                }
            }
        }
    }
}
