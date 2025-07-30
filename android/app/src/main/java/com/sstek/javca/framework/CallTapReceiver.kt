package com.sstek.javca.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sstek.javca.call.domain.entity.Call
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.call.domain.usecase.SendCallRequestUseCase
import com.sstek.javca.call.presentation.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallTapReceiver : BroadcastReceiver() {
    @Inject
    lateinit var sendCallRequestUseCase: SendCallRequestUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val callerId = intent.getStringExtra("callerId") ?: return
        val calleeId = intent.getStringExtra("calleeId") ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val call = Call(
                callerId = callerId,
                calleeId = calleeId,
                timestamp = System.currentTimeMillis(),
                status = CallStatus.PENDING
            )

            val (callId, status) = sendCallRequestUseCase(call)

            val callIntent = Intent(context, CallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("callId", callId)
                putExtra("isCaller", true)
            }
            context.startActivity(callIntent)
        }

    }
}