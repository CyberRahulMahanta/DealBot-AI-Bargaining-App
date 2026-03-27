package com.example.deal.network

import com.example.deal.model.ApiResponse
import com.example.deal.model.BasicResponse
import com.example.deal.model.CartResponse
import com.example.deal.model.CreateOrderRequest
import com.example.deal.model.CreateUserRequest
import com.example.deal.model.OrderResponse
import com.example.deal.model.OrdersResponse
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

    @POST("create_order")
    fun createOrder(@Body request: CreateOrderRequest): Call<OrderResponse>

    @PUT("update_payment_status/{payment_id}")
    fun updatePaymentStatus(
        @Path("payment_id") paymentId: String,
        @Query("status") status: String
    ): Call<Map<String, Any>>

    @GET("orders/{user_id}")
    fun getOrders(
        @Path("user_id") userId: String
    ): Call<OrdersResponse>

    @POST("add_to_cart")
    fun addToCart(
        @Body data: Map<String, @JvmSuppressWildcards Any>
    ): Call<Map<String, String>>

    @GET("cart/{user_id}")
    fun getCart(
        @Path("user_id") userId: String
    ): Call<CartResponse>

    @DELETE("cart/{id}")
    fun deleteCartItem(@Path("id") id: Int): Call<BasicResponse>

    // Delete an order
    @DELETE("users/{user_id}/orders/{order_id}")
    fun deleteOrder(
        @Path("user_id") userId: String,
        @Path("order_id") orderId: Int
    ): Call<ApiResponse>
}