package com.example.tutorial.com.example.tutorial.presentation.feature.symptom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SymptomTag(label: String, severity: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {

    val bg = when (severity) {
        1 -> Color(0xff41b66a)
        2 -> Color(0xfff69e00)
        3 -> Color(0xffda4036)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface)
    }
}
