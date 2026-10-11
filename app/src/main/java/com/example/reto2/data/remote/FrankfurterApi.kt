
package com.example.reto2.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterApi {

    @GET("rates")
    suspend fun getRates(
        @Query("base") base: String = "USD"
    ): List<FrankfurterRate>
}

data class FrankfurterRate(
    val date: String = "",
    val base: String = "",
    val quote: String = "",
    val rate: Double = 0.0
)

object FrankfurterClient {

    private const val BASE_URL = "https://api.frankfurter.dev/v2/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FrankfurterApi by lazy {
        retrofit.create(FrankfurterApi::class.java)
    }
}
