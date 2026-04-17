package com.example.deal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.R
import com.example.deal.model.NotificationItem

class NotificationAdapter(
    private val list: MutableList<NotificationItem>,
    private val onNotificationClick: ((NotificationItem, Int) -> Unit)? = null
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val unreadDot: View = itemView.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = list[position]

        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message
        holder.tvTime.text = formatTime(item.created_at)
        holder.unreadDot.visibility = if (item.is_read) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener {
            onNotificationClick?.invoke(item, position)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<NotificationItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun markItemAsRead(position: Int) {
        if (position in list.indices) {
            val item = list[position]
            if (!item.is_read) {
                list[position] = item.copy(is_read = true)
                notifyItemChanged(position)
            }
        }
    }

    fun markAllAsReadLocally() {
        for (i in list.indices) {
            val item = list[i]
            list[i] = item.copy(is_read = true)
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int): NotificationItem {
        return list[position]
    }

    fun removeItem(position: Int) {
        if (position in list.indices) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun restoreItem(item: NotificationItem, position: Int) {
        list.add(position, item)
        notifyItemInserted(position)
    }

    fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    private fun formatTime(time: String): String {
        return time.replace("T", " ").replace(".000Z", "").take(16)
    }
}