package com.sstek.javca.call_history.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sstek.javca.R
import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.user.domain.entity.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallHistoryAdapter(
    private var currentUserId: String,
    private var allCalls: List<Call>,
    private var usersMap: Map<String, User>,
    private val onCallButtonClick: (calleeId: String) -> Unit
) : RecyclerView.Adapter<CallHistoryAdapter.CallViewHolder>() {

    private var filteredCalls: List<Call> = allCalls

    class CallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val callWithText: TextView = view.findViewById(R.id.callWithText)
        val callTimeText: TextView = view.findViewById(R.id.callTimeText)
        val callStatusIcon: ImageView = view.findViewById(R.id.callStatusIcon)
        val callButton: ImageButton = view.findViewById(R.id.callHistoryCallButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_history, parent, false)
        return CallViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val call = filteredCalls[position]

        val isOutgoing = call.callerId == currentUserId
        val otherUserId = if (isOutgoing) call.calleeId else call.callerId

        val userName = usersMap[otherUserId]?.username ?: "Bilinmeyen"
        holder.callWithText.text = userName

        holder.callTimeText.text = formatTimestamp(holder.itemView.context, call.timestamp)

        val areYouTheCaller = call.callerId == currentUserId

        var icon = R.drawable.timeout
        if (areYouTheCaller) {
            icon = R.drawable.call_outgoing
        } else {
            icon = R.drawable.call_incoming
        }

        var iconTintColor = R.color.black

        when (call.status) {
            CallStatus.ACCEPTED -> {
                iconTintColor = R.color.green
            }
            CallStatus.REJECTED -> {
                iconTintColor = R.color.red
            }
            CallStatus.TIMEOUT -> {
                iconTintColor = R.color.red
            }
            CallStatus.ENDED -> {
                iconTintColor = R.color.green
            }
            CallStatus.PENDING -> {
                iconTintColor = R.color.black
            }
        }

        holder.callStatusIcon.setImageResource(icon)
        holder.callStatusIcon.setColorFilter(
            ContextCompat.getColor(holder.itemView.context, iconTintColor),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        holder.callButton.setOnClickListener {
            onCallButtonClick(otherUserId)
        }
    }

    override fun getItemCount(): Int = filteredCalls.size

    fun updateCalls(newCalls: List<Call>) {
        allCalls = newCalls
        filteredCalls = newCalls
        notifyDataSetChanged()
    }

    fun setUserMap(map: Map<String, User>) {
        usersMap = map
        notifyDataSetChanged()
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val lowerQuery = query.lowercase(Locale.getDefault())
        filteredCalls = if (lowerQuery.isBlank()) {
            allCalls
        } else {
            allCalls.filter { call ->
                val isOutgoing = call.callerId == currentUserId
                val otherUserId = if (isOutgoing) call.calleeId else call.callerId
                val username = usersMap[otherUserId]?.username?.lowercase(Locale.getDefault()) ?: ""
                username.contains(lowerQuery)
            }
        }
        notifyDataSetChanged()
    }

    private fun formatTimestamp(context: Context, timestamp: Long): String {
        val date = Date(timestamp)
        val cal = java.util.Calendar.getInstance().apply { time = date }
        val nowCal = java.util.Calendar.getInstance()

        val isToday = nowCal.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
                nowCal.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)

        nowCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val isYesterday = nowCal.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
                nowCal.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return when {
            isToday -> "${context.getString(R.string.today)}, ${timeFormat.format(date)}"
            isYesterday -> "${context.getString(R.string.yesterday)}, ${timeFormat.format(date)}"
            else -> {
                val fullFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                fullFormat.format(date)
            }
        }
    }
}
