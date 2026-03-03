package com.example.tutorial.com.example.tutorial.presentation.feature.tips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tutorial.com.example.tutorial.domain.model.Article
import com.example.tutorial.com.example.tutorial.presentation.components.ScreenScaffold


@Composable
fun HealthTipsScreen(
    nav: NavController? = null,
    vm: HealthTipsViewModel = hiltViewModel()
) {
    val list by vm.articles.collectAsState()

    ScreenScaffold(title = "Health Tips") { contentPadding ->
        val sysNavBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = 106.dp + sysNavBar
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(list) { art ->
                ArticleCard(article = art) {
                    nav?.navigate("health_tips/detail/${art.id}")
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: Article, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Spacer(Modifier.height(6.dp))
            Text(article.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                article.summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}
