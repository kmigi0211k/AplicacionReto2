
package com.example.reto2.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {

    @GET("forex/rates")
    suspend fun getForexRates(
        @Query("base") base: String,
        @Query("token") token: String
    ): FinnhubForexRatesResponse

    @GET("forex/symbol")
    suspend fun getForexSymbols(
        @Query("exchange") exchange: String,
        @Query("token") token: String
    ): List<ForexSymbolResponse>
}

data class FinnhubForexRatesResponse(
    val base: String = "",
    val quote: Map<String, Double> = emptyMap()
)

data class ForexSymbolResponse(
    val description: String = "",
    val displaySymbol: String = "",
    val symbol: String = ""
)
