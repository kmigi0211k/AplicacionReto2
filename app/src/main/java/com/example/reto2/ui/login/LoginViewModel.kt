
package com.example.reto2.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reto2.data.model.UserProfile
import com.example.reto2.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
    val loading: Boolean = false,
    val user: UserProfile? = null,
    val error: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)

            val result = repository.register(fullName, email, password)

            _uiState.value = result.fold(
                onSuccess = { AuthUiState(user = it) },
                onFailure = {
                    AuthUiState(error = it.message ?: "Error al registrarse.")
                }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)

            val result = repository.login(email, password)

            _uiState.value = result.fold(
                onSuccess = { AuthUiState(user = it) },
                onFailure = {
                    AuthUiState(error = it.message ?: "Error al iniciar sesión.")
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
