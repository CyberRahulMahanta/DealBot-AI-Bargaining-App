package com.example.deal.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.ProductDetailActivity
import com.example.deal.R
import com.example.deal.model.Product
import com.squareup.picasso.Picasso

class FlashSaleAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<FlashSaleAdapter.ViewHolder>() {

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flash_sale, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = productList[position]

        holder.productName.text = product.name ?: "No Name"
        holder.productPrice.text = "₹${product.selling_price}"

        val fullImageUrl = "http://192.168.73.211:8000/${product.image_url}"

        Picasso.get()
            .load(fullImageUrl)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.productImage)

        // ✅ Open product detail
        holder.itemView.setOnClickListener {

            val intent = Intent(holder.itemView.context, ProductDetailActivity::class.java)

            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.selling_price)
            intent.putExtra("product_image", fullImageUrl)

            holder.itemView.context.startActivity(intent)
        }
    }
}