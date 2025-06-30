package com.sstek.javca.presentation.service

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
import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.domain.model.User
import com.sstek.javca.domain.repository.AuthRepository
import com.sstek.javca.domain.repository.CallObserverRepository
import com.sstek.javca.domain.repository.CallRepository
import com.sstek.javca.domain.usecase.GetUserByIdUseCase
import com.sstek.javca.presentation.call.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    lateinit var getUserByIdUseCase: GetUserByIdUseCase


    private var currentCallId: String? = null
    private var ringtone: Ringtone? = null

    private lateinit var currentUser: User

    private val NOTIFICATION_ID = 666

    override fun onCreate() {
        super.onCreate()

        //TODO(It can be problematic without null check)
        currentUser = authRepository.getCurrentUser()!!

        Log.d("CallListenerService", "onCreate() userId = ${currentUser.uid}")
        callObserverRepository.observeIncomingCalls(currentUser.uid) { callId, callRequest ->
            if (callRequest.status == CallStatus.PENDING && currentCallId != callId) {
                CoroutineScope(Dispatchers.IO).launch {
                    val callerUser = getUserByIdUseCase(callRequest.callerId)
                    callerUser?.let {
                        currentCallId = callId
                        showNotification("JAVCA - ${callerUser.username} sizi arıyor.")
                    }
                }
            }

            if (callRequest.status == CallStatus.ACCEPTED) {
                stopRingtone()
                cancelNotification()
                currentCallId = null
                callObserverRepository.removeListener(callId)
            }

            if (callRequest.status == CallStatus.REJECTED) {
                stopRingtone()
                cancelNotification()
                currentCallId = null
                callObserverRepository.removeListener(callId)
            }

            if (callRequest.status == CallStatus.TIMEOUT) {
                stopRingtone()
                cancelNotification()
                currentCallId = null
                callObserverRepository.removeListener(callId)
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun showNotification(message: String) {
        playRingtone()
        val channelId = "call_channel"
        val channelName = "Call Notifications"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Bildirime tıklayınca açılacak aktivite
        val tapIntent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("callerId", message)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Reddet butonu
        val declineIntent = Intent(this, CallListenerService::class.java).apply {
            setAction("ACTION_DECLINE_CALL")
        }
        val declinePendingIntent = PendingIntent.getService(
            this, 1, declineIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Cevapla butonu
        val answerIntent = Intent(this, CallListenerService::class.java).apply {
            setAction("ACTION_ANSWER_CALL")
        }
        val answerPendingIntent = PendingIntent.getService(
            this, 2, answerIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Kanal oluştur (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Bildirim
        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gelen Arama")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_call_end_24, 4)
            .setContentIntent(tapPendingIntent) // Bildirime tıklanınca çalışır
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_accept, "Reddet", declinePendingIntent)
            .addAction(R.drawable.ic_accept, "Cevapla", answerPendingIntent)
            .setAutoCancel(true)

        startForeground(NOTIFICATION_ID, builder.build())
    }


    override fun onBind(intent: Intent?): IBinder? = null

    private fun cancelNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)

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
            }
            "ACTION_ANSWER_CALL" -> {
                stopRingtone()
                cancelNotification()
                val i = Intent(this, CallActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            }
        }
        return START_STICKY
    }

    private fun updateCallStatusInFirebase(callId: String, status: CallStatus) {
        GlobalScope.launch {
            callRepository.updateCallStatus(callId, status)
        }
    }



    private fun playRingtone() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            //ringtone?.play()
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
        callObserverRepository.removeListener(currentCallId.toString())
    }

}