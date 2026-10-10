
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
            val rates = FrankfurterClient.api.getRates(base)

            val quotes = rates.associate { rate ->
                rate.quote to rate.rate
            }

            Result.success(
                FinnhubForexRatesResponse(
                    base = base,
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
