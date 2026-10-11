package com.example.reto2.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reto2.data.model.CurrencyOperation
import com.example.reto2.data.repository.OperationRepository
import kotlinx.coroutines.launch

data class OperationUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isSuccess: Boolean = false
)

data class OperationHistoryUiState(
    val isLoading: Boolean = false,
    val operations: List<CurrencyOperation> = emptyList(),
    val error: String? = null
)

class OperationViewModel : ViewModel() {

    private val repository = OperationRepository()

    var uiState = mutableStateOf(OperationUiState())
        private set

    var historyState = mutableStateOf(OperationHistoryUiState())
        private set

    fun registerOperation(
        type: String,
        baseCurrency: String,
        targetCurrency: String,
        amount: Double,
        referenceRate: Double,
        appliedRate: Double
    ) {
        if (type !in listOf("BUY", "SELL")) {
            uiState.value = OperationUiState(
                message = "El tipo de operación no es válido."
            )
            return
        }

        if (baseCurrency == targetCurrency) {
            uiState.value = OperationUiState(
                message = "Selecciona dos monedas diferentes."
            )
            return
        }

        if (
            !amount.isFinite() ||
            !referenceRate.isFinite() ||
            !appliedRate.isFinite() ||
            amount <= 0 ||
            referenceRate <= 0 ||
            appliedRate <= 0
        ) {
            uiState.value = OperationUiState(
                message = "Verifica la cantidad y las tasas ingresadas."
            )
            return
        }

        viewModelScope.launch {
            uiState.value = OperationUiState(isLoading = true)

            repository.registerOperation(
                type = type,
                baseCurrency = baseCurrency,
                targetCurrency = targetCurrency,
                amount = amount,
                referenceRate = referenceRate,
                appliedRate = appliedRate
            ).fold(
                onSuccess = {
                    uiState.value = OperationUiState(
                        message = "Operación guardada y pendiente de auditoría.",
                        isSuccess = true
                    )

                    loadMyOperations()
                },
                onFailure = { error ->
                    uiState.value = OperationUiState(
                        message = error.message
                            ?: "No fue posible registrar la operación."
                    )
                }
            )
        }
    }

    fun loadMyOperations() {
        viewModelScope.launch {
            historyState.value = historyState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getMyOperations().fold(
                onSuccess = { operations ->
                    historyState.value = OperationHistoryUiState(
                        isLoading = false,
                        operations = operations,
                        error = null
                    )
                },
                onFailure = { error ->
                    historyState.value = historyState.value.copy(
                        isLoading = false,
                        error = error.message
                            ?: "No fue posible cargar el historial."
                    )
                }
            )
        }
    }

    fun clearMessage() {
        uiState.value = OperationUiState()
    }

}