package com.example.tutorial.com.example.tutorial.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = White
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = White
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = White
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = White
    )
)
