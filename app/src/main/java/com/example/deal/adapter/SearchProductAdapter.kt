package com.example.deal.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.ProductDetailActivity
import com.example.deal.R
import com.example.deal.model.Product
import com.example.deal.utils.Constants
import com.squareup.picasso.Picasso
import java.util.Locale

class SearchProductAdapter(products: List<Product>) :
    RecyclerView.Adapter<SearchProductAdapter.ViewHolder>(), Filterable {

    private val originalList = mutableListOf<Product>()
    private val filteredList = mutableListOf<Product>()

    init {
        originalList.addAll(products)
        filteredList.addAll(products)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val productRating: TextView = itemView.findViewById(R.id.productRating)
        val productReviewCount: TextView = itemView.findViewById(R.id.productReviewCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = filteredList[position]

        holder.productName.text = product.name ?: "No Name"
        holder.productPrice.text = "₹${product.selling_price ?: 0}"
        holder.productRating.text = "${product.rating ?: 0.0}"
        holder.productReviewCount.text = "(${product.review_count ?: 0})"

        val imageUrl = Constants.BASE_URL + (product.image_url ?: "")

        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.productImage)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProductDetailActivity::class.java)
            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.selling_price)
            intent.putExtra("product_image", imageUrl)
            holder.itemView.context.startActivity(intent)
        }
    }

    fun updateData(newList: List<Product>) {
        val safeCopy = newList.toList()

        originalList.clear()
        originalList.addAll(safeCopy)

        filteredList.clear()
        filteredList.addAll(safeCopy)

        notifyDataSetChanged()
    }

    fun getFilteredCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""

                val resultList = if (query.isEmpty()) {
                    originalList.toList()
                } else {
                    originalList.filter {
                        it.name?.lowercase(Locale.getDefault())?.contains(query) == true ||
                                it.category?.lowercase(Locale.getDefault())?.contains(query) == true ||
                                it.brand?.lowercase(Locale.getDefault())?.contains(query) == true
                    }
                }

                return FilterResults().apply {
                    values = resultList
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                filteredList.addAll((results?.values as? List<Product>) ?: emptyList())
                notifyDataSetChanged()
            }
        }
    }
}