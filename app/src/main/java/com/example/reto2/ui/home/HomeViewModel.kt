package com.example.reto2.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reto2.data.repository.ForexRepository
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val rates: Map<String, Double> = emptyMap(),
    val error: String? = null,
    val symbolsCount: Int? = null,
    val symbolsError: String? = null,
    val isLoadingSymbols: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val repository = ForexRepository()

    var uiState = mutableStateOf(HomeUiState())
        private set

    fun loadRates() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getRates("USD").fold(
                onSuccess = { response ->
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        rates = response.quote,
                        error = null
                    )
                },
                onFailure = { exception ->
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = exception.localizedMessage
                            ?: "No fue posible consultar las tasas."
                    )
                }
            )
        }
    }

    fun loadSymbols() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isLoadingSymbols = true,
                symbolsError = null,
                symbolsCount = null
            )

            repository.getSymbols().fold(
                onSuccess = { symbols ->
                    uiState.value = uiState.value.copy(
                        isLoadingSymbols = false,
                        symbolsCount = symbols.size,
                        symbolsError = null
                    )
                },
                onFailure = { exception ->
                    uiState.value = uiState.value.copy(
                        isLoadingSymbols = false,
                        symbolsError = exception.localizedMessage
                            ?: "No fue posible consultar los símbolos."
                    )
                }
            )
        }
    }

}