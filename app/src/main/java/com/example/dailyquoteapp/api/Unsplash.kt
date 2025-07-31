package com.example.dailyquoteapp.api

import com.example.dailyquoteapp.BuildConfig
import com.example.dailyquoteapp.model.UnsplashPhoto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    @GET("photos/random")
    fun getRandomImage(
        @Query("query") query: String = "nature",
        @Query("orientation") orientation: String = "portrait",
        @Query("client_id") clientId: String = BuildConfig.UNSPLASH_ACCESS_KEY
    ): Call<UnsplashPhoto>
}
