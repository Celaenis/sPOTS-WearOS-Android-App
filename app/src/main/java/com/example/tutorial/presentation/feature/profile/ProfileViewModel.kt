package com.example.tutorial.com.example.tutorial.presentation.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorial.data.repository.AuthRepository
import com.example.tutorial.data.repository.ProfileRepository
import com.example.tutorial.com.example.tutorial.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val loading: Boolean = false,
    val msg: String? = null,
    val error: String? = null,
    val deleteDone: Boolean = false
)

data class PwdUi(
    val current: String = "",
    val fresh: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val msg: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui

    private val _pwd = MutableStateFlow(PwdUi())
    val pwd: StateFlow<PwdUi> = _pwd

    init {
        repo.profileFlow()
            .onEach { _ui.value = _ui.value.copy(profile = it) }
            .launchIn(viewModelScope)
    }

    fun save(name: String, ageRaw: String) = viewModelScope.launch {
        val age = ageRaw.toIntOrNull()
        if (age == null || age !in 1..120) {
            _ui.value = _ui.value.copy(error = "Age must be 1-120")
            return@launch
        }
        _ui.value = _ui.value.copy(loading = true, error = null)
        repo.updateProfile(name.trim(), age)
            .onSuccess { _ui.value = _ui.value.copy(loading = false, msg = "Saved") }
            .onFailure { _ui.value = _ui.value.copy(loading = false, error = it.message) }
    }

    fun deleteAccount(password: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        repo.deleteAccount(password)
            .onSuccess { _ui.value = _ui.value.copy(deleteDone = true) }
            .onFailure { _ui.value = _ui.value.copy(loading = false, error = it.message) }
    }


    fun updatePwdField(which: String, v: String) {
        _pwd.value = when (which) {
            "current" -> _pwd.value.copy(current = v)
            "fresh" -> _pwd.value.copy(fresh = v)
            else -> _pwd.value.copy(confirm = v)
        }
    }

    fun changePassword() = viewModelScope.launch {
        val s = _pwd.value
        when {
            s.current.isBlank() -> {
                _pwd.value = s.copy(error = "Current password required"); return@launch
            }

            s.fresh.length < 6 -> {
                _pwd.value = s.copy(error = "New password too short"); return@launch
            }

            s.fresh != s.confirm -> {
                _pwd.value = s.copy(error = "Passwords don't match"); return@launch
            }
        }

        _pwd.value = s.copy(loading = true, error = null)
        authRepo.changePassword(s.current, s.fresh)
            .onSuccess { _pwd.value = PwdUi(msg = "Password updated") }
            .onFailure { _pwd.value = s.copy(loading = false, error = it.message) }
    }

}
