package com.example.dailyquoteapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://daily-quote-api-o1xb.onrender.com/"
    private const val BASE_URL_UNSPLASH = "https://api.unsplash.com/"
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: QuoteApi by lazy {
        retrofit.create(QuoteApi::class.java)
    }
    val unsplashApi: UnsplashApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_UNSPLASH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApi::class.java)
    }
}
