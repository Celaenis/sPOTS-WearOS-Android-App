package com.example.tutorial.com.example.tutorial.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.tutorial.com.example.tutorial.presentation.navigation.NavigationItem

@Composable
fun BottomBar(
    currentRoute: String?,
    onItemClick: (NavigationItem) -> Unit
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.HealthTips,
        NavigationItem.MyPage
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}
