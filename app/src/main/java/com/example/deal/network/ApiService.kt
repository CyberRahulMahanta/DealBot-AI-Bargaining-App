package com.example.deal.network

import com.example.deal.model.ApiResponse
import com.example.deal.model.CreateUserRequest
import com.example.deal.model.Product
import com.example.deal.model.ProductResponse
import com.example.deal.model.UserRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("api/products")
    fun getProducts(): Call<List<Product>>

    @GET("api/products/{id}")
    fun getProductById(@Path("id") id: Int): Call<ProductResponse>

    @POST("user")
    fun createUser(@Body user: CreateUserRequest): Call<ApiResponse>

    @GET("user/{uid}")
    fun getUser(@Path("uid") uid: String): Call<ApiResponse>

    // ✅ NEW UPDATE API
    @PUT("user/{uid}")
    fun updateUser(
        @Path("uid") uid: String,
        @Body user: UserRequest
    ): Call<ApiResponse>

    @Multipart
    @POST("user/{uid}/upload-image")
    fun uploadProfileImage(
        @Path("uid") uid: String,
        @Part file: MultipartBody.Part
    ): Call<ApiResponse>
}