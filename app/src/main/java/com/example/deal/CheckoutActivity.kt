package com.example.deal

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout) // Must exist

        val productName = intent.getStringExtra("product_name") ?: "Unknown"
        val productPrice = intent.getDoubleExtra("product_price", 0.0)
        val selectedColor = intent.getStringExtra("selected_color") ?: "N/A"

        Log.d("CHECKOUT", "Received: $productName, $productPrice, $selectedColor")
    }
}
