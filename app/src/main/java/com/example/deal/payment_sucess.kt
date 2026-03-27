package com.example.deal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class payment_sucess : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_sucess)

        val tvAmount: TextView = findViewById(R.id.tvAmount)
        val tvMeta: TextView = findViewById(R.id.tvPaymentMeta)
        val tvSubtitle: TextView = findViewById(R.id.tvSubtitle)
        val chipStatus: Chip = findViewById(R.id.chipStatus)
        val btnMyOrders: MaterialCardView = findViewById(R.id.btnMyOrders)

        val paymentId = intent.getStringExtra("payment_id") ?: "N/A"
        val amount = intent.getDoubleExtra("total_amount", 0.0)
        val status = intent.getStringExtra("payment_status") ?: "Pending"
        val timeMillis = intent.getLongExtra("payment_time", 0L)

        val formattedTime = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            .format(Date(timeMillis))

        tvAmount.text = "₹$amount"
        tvMeta.text = "#$paymentId • $formattedTime"

        tvSubtitle.text = if (status.equals("Success", true)) {
            "Your payment of ₹$amount was successful 🎉"
        } else {
            "Payment failed. Please try again."
        }

        chipStatus.text = status

        if (status.equals("Success", true)) {
            chipStatus.setChipBackgroundColorResource(R.color.success_chip_bg)
            chipStatus.setTextColor(Color.parseColor("#2ECC71"))
        } else {
            chipStatus.setChipBackgroundColorResource(R.color.error_chip_bg)
            chipStatus.setTextColor(Color.RED)
        }

        btnMyOrders.setOnClickListener {
            startActivity(Intent(this, my_orders::class.java))
        }
    }
}