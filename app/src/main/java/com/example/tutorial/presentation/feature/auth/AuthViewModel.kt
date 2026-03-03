package com.example.tutorial.com.example.tutorial.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorial.data.repository.AuthRepository
import com.example.tutorial.data.repository.SensorDataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject


data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val age: String = "",
    val sex: String = "Male",
    val loading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val showPassword: Boolean = false,
    val isRegister: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val sensorRepo: SensorDataRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(
        AuthUiState(isLoggedIn = repo.currentUser != null)
    )
    val ui: StateFlow<AuthUiState> = _ui
    val currentUser get() = repo.currentUser

    fun updateEmail(v: String) {
        _ui.value = _ui.value.copy(email = v)
    }

    fun updatePassword(v: String) {
        _ui.value = _ui.value.copy(password = v)
    }

    fun updateName(v: String) {
        _ui.value = _ui.value.copy(name = v)
    }

    fun updateAge(v: String) {
        _ui.value = _ui.value.copy(age = v)
    }

    fun updateSex(v: String) {
        _ui.value = _ui.value.copy(sex = v)
    }

    fun togglePasswordVisibility() {
        _ui.value = _ui.value.copy(showPassword = !_ui.value.showPassword)
    }

    fun toggleAuthMode() {
        _ui.value = _ui.value.copy(isRegister = !_ui.value.isRegister, error = null)
    }

    fun login() = viewModelScope.launch {
        val err = validateLoginInputs()
        if (err != null) {
            _ui.value = _ui.value.copy(error = err); return@launch
        }

        _ui.value = _ui.value.copy(loading = true, error = null)
        repo.login(_ui.value.email.trim(), _ui.value.password)
            .onSuccess {
                _ui.value = _ui.value.copy(loading = false, isLoggedIn = true)
                sensorRepo.pullRemoteToLocal(auth, firestore)
            }
            .onFailure { _ui.value = _ui.value.copy(loading = false, error = it.message) }
    }

    fun register() = viewModelScope.launch {
        val err = validateRegisterInputs()
        if (err != null) {
            _ui.value = _ui.value.copy(error = err); return@launch
        }

        _ui.value = _ui.value.copy(loading = true, error = null)
        repo.register(
            _ui.value.email.trim(),
            _ui.value.password,
            _ui.value.name.trim(),
            _ui.value.age.toInt(),
            _ui.value.sex
        ).onSuccess {
            _ui.value = _ui.value.copy(loading = false, isLoggedIn = true)
            sensorRepo.pullRemoteToLocal(auth, firestore)
        }
            .onFailure { _ui.value = _ui.value.copy(loading = false, error = it.message) }
    }

    private fun validateLoginInputs(): String? =
        when {
            !isValidEmail(_ui.value.email) -> "Invalid e-mail address"
            else -> null
        }

    private fun validateRegisterInputs(): String? =
        when {
            !isValidEmail(_ui.value.email) -> "Invalid e-mail address"
            _ui.value.name.trim().isEmpty() -> "Name is required"
            _ui.value.age.toIntOrNull() == null -> "Age must be a number"
            _ui.value.age.toInt() !in 1..120 -> "Age must be between 1 and 120"
            else -> null
        }

    private fun isValidEmail(e: String): Boolean =
        Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
        ).matcher(e).find()

    fun logout() {
        repo.logout()
        _ui.value = _ui.value.copy(isLoggedIn = false)
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        kotlin.runCatching {
            repo.resetPassword(email.trim())
        }.onSuccess {
            _ui.value = _ui.value.copy(loading = false, error = "Password reset email sent.")
        }.onFailure {
            _ui.value = _ui.value.copy(loading = false, error = it.localizedMessage)
        }
    }
}
