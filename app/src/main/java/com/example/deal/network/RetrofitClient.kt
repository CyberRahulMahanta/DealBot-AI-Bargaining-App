package com.example.deal.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.deal.network.ApiService

object RetrofitClient {

    private const val BASE_URL = "http://192.168.74.91:8000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}