package com.example.deal.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flash_sale, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.selling_price}"

        val fullImageUrl = "http://192.168.73.139:8000/${product.image_url}"

        Picasso.get()
            .load(fullImageUrl)
            .into(holder.productImage)

        // ✅ Click listener
        holder.itemView.setOnClickListener {

            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java)

            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.selling_price)
            intent.putExtra("product_image", product.image_url)

            context.startActivity(intent)
        }
    }
}