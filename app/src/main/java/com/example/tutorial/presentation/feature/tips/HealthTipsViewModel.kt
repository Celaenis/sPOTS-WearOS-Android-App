package com.example.tutorial.com.example.tutorial.presentation.feature.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorial.com.example.tutorial.domain.model.Article
import com.example.tutorial.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthTipsViewModel @Inject constructor(
    private val repo: ArticleRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        _articles.value = repo.getAll()
    }

    suspend fun getById(id: String): Article? = repo.findById(id)
}