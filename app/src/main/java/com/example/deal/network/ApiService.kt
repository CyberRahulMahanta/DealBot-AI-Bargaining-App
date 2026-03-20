package com.example.deal.network

import com.example.deal.model.ApiResponse
import com.example.deal.model.Product
import com.example.deal.model.ProductResponse
import com.example.deal.model.UserRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("api/products")
    fun getProducts(): Call<List<Product>>

    @GET("api/products/{id}")
    fun getProductById(@Path("id") id: Int): Call<ProductResponse>

    @POST("user")
    fun createUser(@Body user: UserRequest): Call<ApiResponse>

    @GET("user/{uid}")
    fun getUser(@Path("uid") uid: String): Call<ApiResponse>
}