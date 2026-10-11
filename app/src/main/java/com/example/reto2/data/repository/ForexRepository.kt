package com.example.reto2.data.repository

import com.example.reto2.data.remote.FinnhubClient
import com.example.reto2.data.remote.FinnhubForexRatesResponse
import com.example.reto2.data.remote.ForexSymbolResponse
import com.example.reto2.data.remote.FrankfurterClient

class ForexRepository {

    suspend fun getRates(
        base: String = "USD"
    ): Result<FinnhubForexRatesResponse> {
        return try {
            val normalizedBase = base.trim().uppercase()

            if (normalizedBase.isBlank()) {
                return Result.failure(
                    IllegalArgumentException(
                        "Debes especificar una moneda base."
                    )
                )
            }

            val rates = FrankfurterClient.api.getRates(normalizedBase)

            val quotes = rates.associate { rate ->
                rate.quote to rate.rate
            }

            if (quotes.isEmpty()) {
                return Result.failure(
                    IllegalStateException(
                        "El servicio no devolvió tasas de cambio."
                    )
                )
            }

            if (quotes.values.any { !it.isFinite() || it <= 0.0 }) {
                return Result.failure(
                    IllegalStateException(
                        "El servicio devolvió una tasa de cambio no válida."
                    )
                )
            }

            Result.success(
                FinnhubForexRatesResponse(
                    base = normalizedBase,
                    quote = quotes
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSymbols(): Result<List<ForexSymbolResponse>> {
        return try {
            val apiKey = FinnhubClient.apiKey

            if (apiKey.isBlank()) {
                return Result.failure(
                    IllegalStateException(
                        "No se configuró la API Key de Finnhub."
                    )
                )
            }

            val symbols = FinnhubClient.api.getForexSymbols(
                exchange = "OANDA",
                token = apiKey
            )

            Result.success(symbols)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}