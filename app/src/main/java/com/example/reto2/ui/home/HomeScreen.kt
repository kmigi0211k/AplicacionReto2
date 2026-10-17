
package com.example.reto2.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reto2.data.model.CurrencyOperation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val currencies = listOf(
    "USD", "COP", "EUR", "GBP", "JPY",
    "CAD", "CHF", "AUD", "MXN", "BRL"
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    operationViewModel: OperationViewModel = viewModel()
) {
    val state by homeViewModel.uiState
    val operationState by operationViewModel.uiState
    val historyState by operationViewModel.historyState

    var operationType by remember { mutableStateOf("BUY") }
    var baseCurrency by remember { mutableStateOf("USD") }
    var targetCurrency by remember { mutableStateOf("COP") }
    var amountText by remember { mutableStateOf("1") }
    var appliedRateText by remember { mutableStateOf("") }

    var voiceConfirmation by remember {
        mutableStateOf<VoiceCommand?>(null)
    }

    var voiceError by remember {
        mutableStateOf<String?>(null)
    }

    val baseRate = state.rates[baseCurrency] ?: 1.0
    val targetRate = state.rates[targetCurrency] ?: 0.0

    val referenceRate = if (baseRate > 0) {
        targetRate / baseRate
    } else {
        0.0
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val appliedRate = appliedRateText.toDoubleOrNull() ?: 0.0
    val total = amount * appliedRate

    LaunchedEffect(Unit) {
        homeViewModel.loadRates()
        homeViewModel.loadSymbols()
        operationViewModel.loadMyOperations()
    }

    LaunchedEffect(referenceRate) {
        if (referenceRate > 0) {
            appliedRateText = String.format(
                Locale.US,
                "%.6f",
                referenceRate
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Inicio - Divisas") },
                actions = {
                    Button(onClick = onOpenProfile) {
                        Text("Mi perfil")
                    }
                    Button(onClick = onLogout) {
                        Text("Cerrar sesión")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tasas de cambio",
                style = MaterialTheme.typography.titleLarge
            )

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.error?.let { error ->
                Text(
                    text = "Error al cargar las tasas: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text("Monedas disponibles: ${state.rates.size}")

            Button(
                onClick = { homeViewModel.loadRates() },
                enabled = !state.isLoading
            ) {
                Text("Actualizar tasas")
            }

            Button(onClick = onOpenCamera) {
                Text("Abrir cámara")
            }

            // ASISTENTE DE VOZ
            Text(
                text = "Asistente de voz",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Prueba diciendo: \"Quiero comprar 100 dólares\"."
            )

            VoiceAssistantButton { phrase ->
                val command = parseVoiceCommand(phrase)

                if (command != null) {
                    voiceConfirmation = command
                    voiceError = null
                } else {
                    voiceError =
                        "No entendí la orden reconocida: \"$phrase\". " +
                                "Prueba: \"Quiero comprar 100 dólares\"."
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FORMULARIO DE OPERACIONES
            Text(
                text = "Registrar operación",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { operationType = "BUY" },
                    modifier = Modifier.weight(1f),
                    enabled = operationType != "BUY"
                ) {
                    Text("Comprar")
                }

                Button(
                    onClick = { operationType = "SELL" },
                    modifier = Modifier.weight(1f),
                    enabled = operationType != "SELL"
                ) {
                    Text("Vender")
                }
            }

            Text(
                text = if (operationType == "BUY") {
                    "Tipo de operación: Compra"
                } else {
                    "Tipo de operación: Venta"
                }
            )

            CurrencyDropdown(
                label = "Moneda de origen",
                selectedCurrency = baseCurrency,
                onCurrencySelected = { baseCurrency = it }
            )

            CurrencyDropdown(
                label = "Moneda de destino",
                selectedCurrency = targetCurrency,
                onCurrencySelected = { targetCurrency = it }
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Cantidad de $baseCurrency") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = String.format(
                    Locale.US,
                    "%.6f",
                    referenceRate
                ),
                onValueChange = {},
                label = { Text("Tasa de referencia") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true
            )

            OutlinedTextField(
                value = appliedRateText,
                onValueChange = { appliedRateText = it },
                label = { Text("Tasa aplicada") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true
            )

            Text(
                text = "Total estimado: ${
                    String.format(Locale.US, "%.2f", total)
                } $targetCurrency",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {
                    operationViewModel.registerOperation(
                        type = operationType,
                        baseCurrency = baseCurrency,
                        targetCurrency = targetCurrency,
                        amount = amount,
                        referenceRate = referenceRate,
                        appliedRate = appliedRate
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !operationState.isLoading &&
                        amount > 0 &&
                        appliedRate > 0 &&
                        referenceRate > 0 &&
                        baseCurrency != targetCurrency
            ) {
                Text(
                    if (operationState.isLoading) {
                        "Registrando..."
                    } else {
                        "Registrar operación"
                    }
                )
            }

            operationState.message?.let { message ->
                Text(
                    text = message,
                    color = if (operationState.isSuccess) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FINNHUB
            Text(
                text = "Símbolos de Finnhub",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = { homeViewModel.loadSymbols() },
                enabled = !state.isLoadingSymbols
            ) {
                Text(
                    if (state.isLoadingSymbols) {
                        "Consultando..."
                    } else {
                        "Consultar símbolos"
                    }
                )
            }

            if (state.isLoadingSymbols) {
                CircularProgressIndicator()
            }

            Text("Símbolos encontrados: ${state.symbolsCount ?: 0}")

            state.symbolsError?.let { error ->
                Text(
                    text = "Error de Finnhub: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // HISTORIAL
            Text(
                text = "Mi historial de operaciones",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Aquí puedes consultar las operaciones que has registrado."
            )

            Button(
                onClick = { operationViewModel.loadMyOperations() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !historyState.isLoading
            ) {
                Text(
                    if (historyState.isLoading) {
                        "Actualizando historial..."
                    } else {
                        "Actualizar historial"
                    }
                )
            }

            if (historyState.isLoading) {
                CircularProgressIndicator()
            }

            historyState.error?.let { error ->
                Text(
                    text = "Error al cargar el historial: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (
                !historyState.isLoading &&
                historyState.error == null &&
                historyState.operations.isEmpty()
            ) {
                Text("Todavía no tienes operaciones registradas.")
            }

            historyState.operations.forEach { operation ->
                OperationHistoryCard(operation = operation)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // CONFIRMAR ORDEN RECONOCIDA
    voiceConfirmation?.let { command ->
        AlertDialog(
            onDismissRequest = {
                voiceConfirmation = null
            },
            title = {
                Text("Confirmar orden de voz")
            },
            text = {
                Text(
                    "Operación: ${
                        if (command.operationType == "BUY") {
                            "Comprar"
                        } else {
                            "Vender"
                        }
                    }\n" +
                            "Cantidad: ${command.amount} ${command.baseCurrency}\n" +
                            "Moneda de destino: ${command.targetCurrency}\n\n" +
                            "¿Quieres completar el formulario con estos datos?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        operationType = command.operationType
                        baseCurrency = command.baseCurrency
                        targetCurrency = command.targetCurrency
                        amountText = command.amount.toString()
                        voiceConfirmation = null
                    }
                ) {
                    Text("Completar formulario")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        voiceConfirmation = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ERROR DE RECONOCIMIENTO
    voiceError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                voiceError = null
            },
            title = {
                Text("No se reconoció la orden")
            },
            text = {
                Text(error)
            },
            confirmButton = {
                Button(
                    onClick = {
                        voiceError = null
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun OperationHistoryCard(
    operation: CurrencyOperation
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (operation.type == "BUY") {
                    "Compra de divisas"
                } else {
                    "Venta de divisas"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "Monedas: ${operation.baseCurrency} → ${operation.targetCurrency}"
            )

            Text(
                "Cantidad: ${
                    String.format(
                        Locale.US,
                        "%.2f",
                        operation.amount
                    )
                } ${operation.baseCurrency}"
            )

            Text(
                "Tasa de referencia: ${
                    String.format(
                        Locale.US,
                        "%.6f",
                        operation.referenceRate
                    )
                }"
            )

            Text(
                "Tasa aplicada: ${
                    String.format(
                        Locale.US,
                        "%.6f",
                        operation.appliedRate
                    )
                }"
            )

            Text(
                "Total: ${
                    String.format(
                        Locale.US,
                        "%.2f",
                        operation.total
                    )
                } ${operation.targetCurrency}"
            )

            Text(
                text = when (operation.status) {
                    "PENDING_AUDIT" -> "Estado: Pendiente de auditoría"
                    "APPROVED" -> "Estado: Aprobada"
                    "REJECTED" -> "Estado: Rechazada"
                    else -> "Estado: ${operation.status}"
                }
            )

            Text(
                text = "Fecha: ${
                    SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date(operation.createdAt))
                }",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CurrencyDropdown(
    label: String,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )

        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedCurrency)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}
