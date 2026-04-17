package com.example.deal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.adapter.NotificationAdapter
import com.example.deal.model.BasicResponse
import com.example.deal.model.NotificationItem
import com.example.deal.model.NotificationResponse
import com.example.deal.network.RetrofitClient
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnBack: MaterialCardView
    private lateinit var tvMarkAllRead: TextView

    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<NotificationItem>()

    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = firebaseUser.uid

        initViews()
        setupRecycler()
        setupSwipeToDelete()

        btnBack.setOnClickListener {
            finish()
        }

        tvMarkAllRead.setOnClickListener {
            markAllNotificationsRead()
        }

        loadNotifications()
    }

    private fun initViews() {
        rvNotifications = findViewById(R.id.rvNotifications)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnBack = findViewById(R.id.btnBack)
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead)
    }

    private fun setupRecycler() {
        adapter = NotificationAdapter(notificationList) { item, position ->
            if (!item.is_read) {
                markSingleNotificationRead(item.id, position)
            }
        }

        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun setupSwipeToDelete() {
        val deleteIcon = getDrawable(R.drawable.delete_icon_white)
        val iconMargin = 32
        val background = android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = 32f
            setColor(android.graphics.Color.parseColor("#EF4444"))
        }

        val itemTouchHelperCallback =
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0,
                androidx.recyclerview.widget.ItemTouchHelper.RIGHT
            ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val item = adapter.getItem(position)
                    deleteNotification(item.id, position, item)
                }

                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                    return 0.35f
                }

                override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                    return defaultValue * 1.5f
                }

                override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
                    return defaultValue * 1.5f
                }

                override fun onChildDraw(
                    c: android.graphics.Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView

                    if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) {
                        val backgroundLeft = itemView.left + 12
                        val backgroundTop = itemView.top + 8
                        val backgroundRight = itemView.left + dX.toInt()
                        val backgroundBottom = itemView.bottom - 8

                        background.setBounds(
                            backgroundLeft,
                            backgroundTop,
                            backgroundRight,
                            backgroundBottom
                        )
                        background.draw(c)

                        deleteIcon?.let { icon ->
                            val iconSize = 80

                            val iconLeft = itemView.left + iconMargin
                            val iconRight = iconLeft + iconSize

                            val iconTop = itemView.top + (itemView.height - iconSize) / 2
                            val iconBottom = iconTop + iconSize

                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            icon.draw(c)
                        }

                        val maxSwipe = recyclerView.width * 0.7f
                        val limitedDX = dX.coerceAtMost(maxSwipe)

                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            limitedDX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    } else {
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                }
            }

        androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback)
            .attachToRecyclerView(rvNotifications)
    }

    private fun loadNotifications() {
        RetrofitClient.api.getNotifications(userId)
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(
                    call: Call<NotificationResponse>,
                    response: Response<NotificationResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!

                        if (body.success && body.data.isNotEmpty()) {
                            adapter.updateData(body.data)
                            showList()
                        } else {
                            showEmptyState()
                        }
                    } else {
                        showEmptyState()
                    }
                }

                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    showEmptyState()
                }
            })
    }

    private fun markSingleNotificationRead(notificationId: Int, position: Int) {
        RetrofitClient.api.markNotificationAsRead(notificationId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        adapter.markItemAsRead(position)
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                }
            })
    }

    private fun markAllNotificationsRead() {
        RetrofitClient.api.markAllNotificationsAsRead(userId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        adapter.markAllAsReadLocally()
                        Toast.makeText(
                            this@NotificationsActivity,
                            response.body()?.message ?: "All marked as read",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@NotificationsActivity,
                            "Failed to mark all as read",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun deleteNotification(notificationId: Int, position: Int, item: NotificationItem) {
        RetrofitClient.api.deleteNotification(notificationId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        adapter.removeItem(position)

                        if (adapter.isEmpty()) {
                            showEmptyState()
                        }

                        Toast.makeText(
                            this@NotificationsActivity,
                            "Notification deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        rvNotifications.post {
                            adapter.notifyItemChanged(position)
                        }
                        Toast.makeText(
                            this@NotificationsActivity,
                            "Failed to delete notification",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    rvNotifications.post {
                        adapter.notifyItemChanged(position)
                    }
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showEmptyState() {
        rvNotifications.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
    }

    private fun showList() {
        rvNotifications.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
    }
}