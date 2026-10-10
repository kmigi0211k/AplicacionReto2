
package com.example.reto2.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel
) {
    val state by homeViewModel.uiState

    LaunchedEffect(Unit) {
        homeViewModel.loadRates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inicio - Divisas",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(onClick = onLogout) {
                Text("Cerrar sesión")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tasas de cambio",
            style = MaterialTheme.typography.titleLarge
        )

        when {
            state.isLoading -> {
                CircularProgressIndicator()
                Text("Consultando tasas...")
            }

            state.error != null -> {
                Text(
                    text = "Error al consultar tasas: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )

                Button(onClick = { homeViewModel.loadRates() }) {
                    Text("Reintentar tasas")
                }
            }

            state.rates.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.rates.toList().sortedBy { it.first }) { rate ->
                        Text("${rate.first}: ${rate.second}")
                    }
                }
            }

            else -> {
                Text("No hay tasas disponibles.")
                Button(onClick = { homeViewModel.loadRates() }) {
                    Text("Consultar tasas")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Prueba de símbolos de Finnhub",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Esta prueba consulta los pares de divisas disponibles."
        )

        Button(
            onClick = { homeViewModel.loadSymbols() },
            enabled = !state.isLoadingSymbols
        ) {
            Text(
                if (state.isLoadingSymbols) {
                    "Consultando símbolos..."
                } else {
                    "Probar símbolos"
                }
            )
        }

        if (state.isLoadingSymbols) {
            CircularProgressIndicator()
        }

        state.symbolsCount?.let { count ->
            Text(
                text = "Consulta exitosa. Símbolos recibidos: $count",
                color = MaterialTheme.colorScheme.primary
            )
        }

        state.symbolsError?.let { error ->
            Text(
                text = "Error al consultar símbolos: $error",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
