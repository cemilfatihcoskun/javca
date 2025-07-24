package com.sstek.javca.framework

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sstek.javca.R
import com.sstek.javca.call.domain.entity.CallRequest
import com.sstek.javca.call.domain.entity.CallStatus
import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.auth.domain.repository.AuthRepository
import com.sstek.javca.call.domain.repository.CallObserverRepository
import com.sstek.javca.call.domain.repository.CallRepository
import com.sstek.javca.auth.domain.usecase.GetCurrentUserUseCase
import com.sstek.javca.user.domain.usecase.GetUserByIdUseCase
import com.sstek.javca.call.domain.usecase.UpdateCallRequestUseCase
import com.sstek.javca.call.presentation.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class CallListenerService : Service() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var callRepository: CallRepository

    @Inject
    lateinit var callObserverRepository: CallObserverRepository

    @Inject
    lateinit var updateCallRequestUseCase: UpdateCallRequestUseCase

    @Inject
    lateinit var getUserByIdUseCase: GetUserByIdUseCase

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    private var currentUser: User? = null
    private var currentCallId: String? = null
    private var ringtone: Ringtone? = null

    private val PENDING_NOTIFICATION_ID = 666
    private val TIMEOUT_NOTIFICATION_ID = 667

    private val SERVICE_START_TIME = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            currentUser = getCurrentUserUseCase()

            if (currentUser == null) {
                Log.e("CallListenerService", "Kullanıcı alınamadı, servis kapatılıyor.")
                stopSelf()
                return@launch
            }

            Log.d("CallListenerService", "onCreate() userId = ${currentUser?.uid}")

            callObserverRepository.observeIncomingCalls(currentUser!!.uid) { callId, callRequest ->
                if (callRequest.status == CallStatus.PENDING && callRequest.callerId != currentUser!!.uid && currentCallId != callId) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val callerUser = getUserByIdUseCase(callRequest.callerId)
                        callerUser?.let {
                            currentCallId = callId
                            showNotification("JAVCA - ${callerUser.username} sizi arıyor.")
                        }
                    }
                }

                if (callRequest.status == CallStatus.ACCEPTED ||
                    callRequest.status == CallStatus.REJECTED ||
                    callRequest.status == CallStatus.TIMEOUT) {

                    stopRingtone()
                    cancelNotification()
                    currentCallId = null
                }

                if (callRequest.status == CallStatus.TIMEOUT && callRequest.callerId != currentUser!!.uid) {
                    if (SERVICE_START_TIME < callRequest.timestamp) {
                        showTimeoutNotification(callRequest)
                    }
                }
            }
        }
    }

    private fun getDateTimeStr(timestamps: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH.mm.SS")
            val netDate = Date(timestamps)
            sdf.format(netDate)
        } catch (e: Exception) {
            Log.e("CallListenerService", "getDateTimeStr() ${e.message}")
            "29.05.1453 12.00.00"
        }
    }

    private fun showTimeoutNotification(callRequest: CallRequest) {
        val channelId = "call_channel"
        val channelName = "Call Notifications"

        CoroutineScope(Dispatchers.IO).launch {
            val callerUser = getUserByIdUseCase(callRequest.callerId)
            val caller = callerUser?.username ?: "Bilinmeyen"
            val datetime = getDateTimeStr(callRequest.timestamp)
            val message = "$caller sizi $datetime tarihinde aradı. Aramak için tıklayınız."

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val tapIntent = Intent(this@CallListenerService, CallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("callId", true)
                putExtra("isCaller", true)
            }
            val tapPendingIntent = PendingIntent.getActivity(
                this@CallListenerService, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                manager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(this@CallListenerService, channelId)
                .setContentTitle("Gelen Arama")
                .setContentText(message)
                .setSmallIcon(R.drawable.apple_touch_icon)
                .setContentIntent(tapPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)

            manager.notify(TIMEOUT_NOTIFICATION_ID, builder.build())
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun showNotification(message: String) {
        playRingtone()
        val channelId = "call_channel"
        val channelName = "Call Notifications"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val declineIntent = Intent(this, CallListenerService::class.java).apply {
            action = "ACTION_DECLINE_CALL"
        }
        val declinePendingIntent = PendingIntent.getService(
            this, 1, declineIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = Intent(this, CallListenerService::class.java).apply {
            action = "ACTION_ANSWER_CALL"
        }
        val answerPendingIntent = PendingIntent.getService(
            this, 2, answerIntent, PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC  // kilit ekranı görünürlüğü
            manager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("callId", currentCallId)
            putExtra("isCaller", false)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gelen Arama")
            .setContentText(message)
            .setSmallIcon(R.drawable.apple_touch_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_reject, "Reddet", declinePendingIntent)
            .addAction(R.drawable.ic_accept, "Cevapla", answerPendingIntent)
            .setAutoCancel(true)

        startForeground(PENDING_NOTIFICATION_ID, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun cancelNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(PENDING_NOTIFICATION_ID)
        stopForeground(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_DECLINE_CALL" -> {
                stopRingtone()
                cancelNotification()
                currentCallId?.let { id ->
                    updateCallStatusInFirebase(id, CallStatus.REJECTED)
                }
                Log.d("CallListenerService", "call rejected")
            }
            "ACTION_ANSWER_CALL" -> {
                stopRingtone()
                cancelNotification()
                currentCallId?.let { id ->
                    updateCallStatusInFirebase(id, CallStatus.ACCEPTED)
                    val i = Intent(this, CallActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    i.putExtra("isCaller", false)
                    i.putExtra("callId", id)
                    startActivity(i)
                }
            }
        }
        return START_STICKY
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateCallStatusInFirebase(callId: String, status: CallStatus) {
        GlobalScope.launch {
            callRepository.updateCallStatus(callId, status)
        }
    }

    private fun playRingtone() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        try {
            ringtone?.stop()
            ringtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        cancelNotification()
        currentUser?.let {
            callObserverRepository.removeListener(it.uid)
        }
    }
}
