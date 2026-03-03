package com.example.tutorial.com.example.tutorial.presentation.feature.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorial.data.local.EpisodeEntity
import com.example.tutorial.data.repository.EpisodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val repo: EpisodeRepository
) : ViewModel() {

    private val _episodes = MutableStateFlow<List<EpisodeEntity>>(emptyList())
    val episodes: StateFlow<List<EpisodeEntity>> = _episodes

    init {
        viewModelScope.launch {
            repo.pullRemoteToLocal()
            _episodes.value = repo.recent()
        }
    }

    fun refresh() = viewModelScope.launch {
        repo.pullRemoteToLocal()
        _episodes.value = repo.recent()
    }
}
