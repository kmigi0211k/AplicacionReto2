
package com.example.reto2.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reto2.data.model.CurrencyOperation
import com.example.reto2.data.repository.OperationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: OperationRepository
) {
    var operations by remember {
        mutableStateOf<List<CurrencyOperation>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(repository) {
        loading = true
        errorMessage = null

        repository.getMyOperations().fold(
            onSuccess = { result ->
                operations = result.sortedByDescending {
                    it.createdAt
                }
            },
            onFailure = { error ->
                errorMessage = error.localizedMessage
                    ?: "No se pudo cargar el historial."
            }
        )

        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Historial de operaciones")
                }
            )
        }
    ) { innerPadding ->

        when {
            loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()

                    Text(
                        text = "Cargando historial...",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                        .padding(20.dp)
                )
            }

            operations.isEmpty() -> {
                Text(
                    text = "Todavía no tienes operaciones registradas.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                        .padding(20.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = operations,
                        key = {
                            it.id.ifBlank {
                                "${it.userId}_${it.createdAt}"
                            }
                        }
                    ) { operation ->
                        OperationCard(operation)
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationCard(
    operation: CurrencyOperation
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (operation.type) {
                    "BUY" -> "Compra"
                    "SELL" -> "Venta"
                    else -> operation.type
                },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Monedas: ${operation.baseCurrency} → " +
                        operation.targetCurrency
            )

            Text(text = "Cantidad: ${operation.amount}")

            Text(
                text = "Tasa de referencia: ${operation.referenceRate}"
            )

            Text(
                text = "Tasa aplicada: ${operation.appliedRate}"
            )

            Text(text = "Total: ${operation.total}")

            Text(text = "Estado: ${operation.status}")

            Text(
                text = "Fecha: ${formatOperationDate(operation.createdAt)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatOperationDate(
    timestamp: Long
): String {
    if (timestamp <= 0L) {
        return "Fecha no disponible"
    }

    val formatter = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.forLanguageTag("es-CO")
    )

    formatter.timeZone = TimeZone.getTimeZone("America/Bogota")

    return formatter.format(Date(timestamp))
}
