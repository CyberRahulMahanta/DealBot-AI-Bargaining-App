package com.example.deal.network

import ApiResponseGeneric
import BargainRequest
import NegotiationMessageData
import StartNegotiationData
import StartNegotiationRequest
import com.example.deal.model.ApiResponse
import com.example.deal.model.BasicResponse
import com.example.deal.model.CartResponse
import com.example.deal.model.CouponValidateResponse
import com.example.deal.model.CreateOrderRequest
import com.example.deal.model.CreateUserRequest
import com.example.deal.model.NotificationRequest
import com.example.deal.model.NotificationResponse
import com.example.deal.model.OrderResponse
import com.example.deal.model.OrdersResponse
import com.example.deal.model.Product
import com.example.deal.model.ProductResponse
import com.example.deal.model.ShippingMethodsResponse
import com.example.deal.model.UnreadCountResponse
import com.example.deal.model.UserRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("api/products")
    fun getProducts(): Call<List<Product>>

    @GET("api/products")
    fun getAllProducts(): Call<List<Product>>

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

    @POST("api/negotiation/start")
    fun startNegotiation(
        @Body request: StartNegotiationRequest
    ): Call<ApiResponseGeneric<StartNegotiationData>>

    @POST("api/negotiation/message")
    fun sendNegotiationMessage(
        @Body request: BargainRequest
    ): Call<ApiResponseGeneric<NegotiationMessageData>>

    @GET("api/notifications/{user_id}")
    fun getNotifications(
        @Path("user_id") userId: String
    ): Call<NotificationResponse>

    @PUT("api/notifications/read/{notification_id}")
    fun markNotificationAsRead(
        @Path("notification_id") notificationId: Int
    ): Call<BasicResponse>

    @PUT("api/notifications/read-all/{user_id}")
    fun markAllNotificationsAsRead(
        @Path("user_id") userId: String
    ): Call<BasicResponse>

    @GET("api/notifications/unread-count/{user_id}")
    fun getUnreadNotificationCount(
        @Path("user_id") userId: String
    ): Call<UnreadCountResponse>

    @DELETE("api/notifications/{notification_id}")
    fun deleteNotification(
        @Path("notification_id") notificationId: Int
    ): Call<BasicResponse>

    @POST("add_notification")
    fun addNotification(@Body request: NotificationRequest): Call<Map<String, String>>

    @GET("api/shipping-methods")
    fun getShippingMethods(): Call<ShippingMethodsResponse>

    @GET("api/coupons/validate")
    fun validateCoupon(
        @Query("code") code: String
    ): Call<CouponValidateResponse>
}