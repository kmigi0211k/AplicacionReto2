
package com.example.reto2.data.remote

import com.example.reto2.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FinnhubClient {

    private const val BASE_URL = "https://finnhub.io/api/v1/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FinnhubApi by lazy {
        retrofit.create(FinnhubApi::class.java)
    }

    val apiKey: String
        get() = BuildConfig.FINNHUB_API_KEY
}
