package com.example.tutorial.com.example.tutorial.presentation.feature.article

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tutorial.com.example.tutorial.presentation.feature.tips.HealthTipsViewModel
import com.example.tutorial.com.example.tutorial.presentation.components.ScreenScaffold
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle


@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    paragraphSpacing: Int = 10
) {
    val lines = text.trim().lines()
    var insideList = false

    Column(modifier) {
        lines.forEachIndexed { i, line ->
            when {
                line.startsWith(">") -> {
                    MarkdownInlineText(
                        line.removePrefix(">").trim(),
                        baseStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        ),
                        italicInQuote = true
                    )
                }
                line.matches(Regex("^\\s*[-*]\\s+.*")) -> {
                    Row(Modifier.padding(start = 12.dp, bottom = 2.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold)
                        MarkdownInlineText(line.replace(Regex("^\\s*[-*]\\s+"), ""))
                    }
                    insideList = true
                }
                line.matches(Regex("^\\s*\\d+\\.\\s+.*")) -> {
                    val (number, content) = line.trim().split(".", limit = 2)
                    Row(Modifier.padding(start = 12.dp, bottom = 2.dp)) {
                        Text("$number. ", fontWeight = FontWeight.Bold)
                        MarkdownInlineText(content.trim())
                    }
                    insideList = true
                }
                line.isBlank() -> {
                    if (i > 0 && !lines[i - 1].isBlank())
                        Spacer(Modifier.height(paragraphSpacing.dp))
                    insideList = false
                }
                else -> {
                    MarkdownInlineText(line.trim())
                    insideList = false
                }
            }
        }
    }
}

@Composable
fun MarkdownInlineText(
    text: String,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    italicInQuote: Boolean = false
) {
    Text(
        text = buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                when {
                    text.startsWith("**", i) -> {
                        val end = text.indexOf("**", i + 2)
                        if (end != -1) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(text.substring(i + 2, end))
                            }
                            i = end + 2
                        } else {
                            append("*")
                            i++
                        }
                    }
                    text.startsWith("_", i) -> {
                        val end = text.indexOf("_", i + 1)
                        if (end != -1) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(text.substring(i + 1, end))
                            }
                            i = end + 1
                        } else {
                            append("_")
                            i++
                        }
                    }
                    else -> {
                        append(text[i])
                        i++
                    }
                }
            }
        },
        style = if (italicInQuote) baseStyle else baseStyle
    )
}


@Composable
fun ArticleDetailScreen(
    articleId: String,
    vm: HealthTipsViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("…") }
    var body  by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(articleId) {
        scope.launch {
            vm.getById(articleId)?.let {
                title = it.title
                body = it.content
            }
        }
    }

    ScreenScaffold(title = title) { padd ->
        Column(
            modifier = Modifier
                .padding(padd)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MarkdownText(body)
            Spacer(Modifier.height(64.dp))
        }
    }
}
