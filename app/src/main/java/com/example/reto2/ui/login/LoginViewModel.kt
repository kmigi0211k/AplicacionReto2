
package com.example.reto2.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reto2.data.model.UserProfile
import com.example.reto2.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun register(
        fullName: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            Log.d("LOGIN_DEBUG", "Se ejecuto register()")
            _uiState.value = AuthUiState(loading = true)

            try {
                val result = repository.register(
                    fullName = fullName,
                    email = email,
                    password = password
                )

                _uiState.value = result.fold(
                    onSuccess = { profile ->
                        Log.d("LOGIN_DEBUG", "Registro exitoso")
                        AuthUiState(user = profile)
                    },
                    onFailure = { error ->
                        Log.e("LOGIN_DEBUG", "Error en registro", error)
                        AuthUiState(
                            error = error.message ?: "Error al registrarse."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LOGIN_DEBUG", "Excepcion inesperada al registrar", e)
                _uiState.value = AuthUiState(
                    error = e.message ?: "Error inesperado al registrarse."
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d("LOGIN_DEBUG", "Se ejecuto login()")
            _uiState.value = AuthUiState(loading = true)

            try {
                Log.d("LOGIN_DEBUG", "Iniciando llamada al repositorio")

                val result = repository.login(
                    email = email,
                    password = password
                )

                _uiState.value = result.fold(
                    onSuccess = { profile ->
                        Log.d(
                            "LOGIN_DEBUG",
                            "Login exitoso. Rol: ${profile.role}"
                        )
                        AuthUiState(user = profile)
                    },
                    onFailure = { error ->
                        Log.e("LOGIN_DEBUG", "Error en login", error)
                        AuthUiState(
                            error = error.message
                                ?: "Error al iniciar sesión."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LOGIN_DEBUG", "Excepcion inesperada en login", e)
                _uiState.value = AuthUiState(
                    error = e.message
                        ?: "Error inesperado al iniciar sesión."
                )
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
        Log.d("LOGIN_DEBUG", "Sesion cerrada")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
