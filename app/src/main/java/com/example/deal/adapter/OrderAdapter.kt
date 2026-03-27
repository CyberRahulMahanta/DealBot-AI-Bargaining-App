package com.example.deal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.deal.model.ApiResponse
import com.example.deal.model.OrderItem
import com.example.deal.network.RetrofitClient
import com.example.deal.utils.Constants
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderAdapter(
    private val list: MutableList<OrderItem>,
    private val userId: String   // ✅ Added
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.imgProduct)

        val name: TextView = view.findViewById(R.id.tvProductName)
        val amount: TextView = view.findViewById(R.id.tvAmount)
        val paymentId: TextView = view.findViewById(R.id.tvPaymentId)
        val time: TextView = view.findViewById(R.id.tvTime)
        val status: Chip = view.findViewById(R.id.chipStatus)
        val deleteBtn: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.product_name
        holder.amount.text = "₹${item.total_amount}"
        holder.paymentId.text = "Payment ID: ${item.payment_id}"
        holder.time.text = item.payment_time
        holder.status.text = item.payment_status

        val imageUrl = Constants.BASE_URL + item.product_image

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.productImage)

        // Status color
        if (item.payment_status.equals("Success", true)) {
            holder.status.setChipBackgroundColorResource(R.color.success_chip_bg)
        } else {
            holder.status.setChipBackgroundColorResource(R.color.error_chip_bg)
        }

        // ✅ DELETE CLICK
        holder.deleteBtn.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Order")
                .setMessage("Are you sure you want to delete this order?")
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteOrder(item.id, position, holder)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    // ✅ UPDATED DELETE FUNCTION
    private fun deleteOrder(orderId: Int, position: Int, holder: ViewHolder) {

        holder.deleteBtn.isEnabled = false // prevent double click

        RetrofitClient.api.deleteOrder(userId, orderId)
            .enqueue(object : Callback<ApiResponse> {

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {

                    holder.deleteBtn.isEnabled = true

                    if (response.isSuccessful && response.body()?.success == true) {

                        list.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, list.size)

                        Toast.makeText(holder.itemView.context, "Order deleted", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(holder.itemView.context, "Failed to delete order", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {

                    holder.deleteBtn.isEnabled = true
                    Toast.makeText(holder.itemView.context, "API Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}