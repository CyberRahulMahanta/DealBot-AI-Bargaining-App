package com.example.deal.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.deal.R
import com.example.deal.model.CartItem
import com.example.deal.utils.Constants

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val listener: OnQuantityChangeListener
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    interface OnQuantityChangeListener {
        fun onQuantityChanged(updatedList: List<CartItem>)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvTitle)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val image: ImageView = view.findViewById(R.id.ivProduct)
        val quantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnPlus: ImageButton = view.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = view.findViewById(R.id.btnMinus)
        val colorView: View = view.findViewById(R.id.viewColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.quantity.text = item.quantity.toString()
        holder.price.text = "₹ ${item.selling_price * item.quantity}"

        Glide.with(holder.itemView.context)
            .load(Constants.BASE_URL + item.image_url)
            .into(holder.image)

        // Color circle with black border
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.parseColor(item.selected_color ?: "#000000"))
        drawable.setStroke(2, Color.BLACK)
        holder.colorView.background = drawable

        // PLUS
        holder.btnPlus.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            listener.onQuantityChanged(items)
        }

        // MINUS
        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
                listener.onQuantityChanged(items)
            }
        }
    }

    override fun getItemCount() = items.size
}