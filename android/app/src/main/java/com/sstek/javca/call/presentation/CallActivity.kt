package com.sstek.javca.call.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.window.OnBackInvokedDispatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

import com.google.android.material.snackbar.Snackbar
import com.sstek.javca.databinding.ActivityCallBinding
import com.sstek.javca.call.domain.entity.CallStatus
import org.webrtc.SurfaceViewRenderer

import com.sstek.javca.R

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {
    private val viewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding

    private var callId: String? = null

    private var dX = 0f
    private var dY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CallActivity", "onCreate() called. Intent: ${intent.extras}")

        // TODO(Deprecated android in daha ileri seviyeleri için bunu güncelle)
        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val localRenderer: SurfaceViewRenderer = binding.localVideoSurfaceViewRenderer
        val remoteRenderer: SurfaceViewRenderer = binding.remoteVideoSurfaceViewRenderer

        val isCaller = intent.getBooleanExtra("isCaller", false)
        callId = intent.getStringExtra("callId")

        if (callId == null) {
            Log.d("CallActivity", "onCreate() callId cannot be null")
            finish()
        }

        viewModel.initWebRtcManager(callId.toString(), this, localRenderer, remoteRenderer)

        if (isCaller) {
            viewModel.createOffer()
        }

        viewModel.callStatus.observe(this) { status ->
            when (status) {
                CallStatus.PENDING -> {
                    Log.d("CallActivity", "pending")
                    Snackbar.make(binding.root, "Aranıyor.", Snackbar.LENGTH_SHORT).show()
                }

                CallStatus.ACCEPTED -> {

                }

                CallStatus.REJECTED -> {
                    Log.d("CallActivity", "Rejected")

                    val snackbar =
                        Snackbar.make(binding.root, "Arama reddedildi", Snackbar.LENGTH_SHORT)
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            viewModel.cleanWebRtc()
                            finish()
                        }
                    })
                    snackbar.show()
                }

                CallStatus.TIMEOUT -> {
                    Log.d("CallActivity", "Timeout")

                    val snackbar = Snackbar.make(
                        binding.root,
                        "Aradığınız kişiye ulaşılamadı.",
                        Snackbar.LENGTH_SHORT
                    )
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            viewModel.cleanWebRtc()
                            finish()
                        }
                    })
                    snackbar.show()
                }

                CallStatus.ENDED -> {
                    Log.d("CallActivity", "Arama bitti.")

                    val snackbar =
                        Snackbar.make(binding.root, "Arama bitti.", Snackbar.LENGTH_SHORT)
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            viewModel.cleanWebRtc()
                            finish()
                        }
                    })
                    snackbar.show()
                }
            }
        }


        binding.toggleMicrophoneButton.setOnClickListener {
            val enabled = viewModel.toggleMicrophone()
            if (enabled) {
                binding.toggleMicrophoneButton.setImageResource(R.drawable.ic_baseline_mic_24)
            } else {
                binding.toggleMicrophoneButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            }
        }

        binding.toggleVideoButton.setOnClickListener {
            val enabled = viewModel.toggleVideo()
            if (enabled) {
                binding.toggleVideoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            } else {
                binding.toggleVideoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            }
        }

        binding.switchCameraButton.setOnClickListener {
            viewModel.switchCamera()
        }

        binding.endCallButton.setOnClickListener {
            viewModel.endCall(callId!!)
            finish()
        }

        binding.localVideoSurfaceViewRenderer.setOnTouchListener(object : View.OnTouchListener {
            private var dX = 0f
            private var dY = 0f
            private var isMoving = false
            private val touchSlop = ViewConfiguration.get(this@CallActivity).scaledTouchSlop
            private var downX = 0f
            private var downY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val parent = binding.callLayout
                val controls = binding.controls
                val padding = (16 * resources.displayMetrics.density)
                val minX = padding
                val minY = padding
                val maxX = parent.width - v.width - padding
                val maxY = controls.top.toFloat() - v.height - padding

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = v.x - event.rawX
                        dY = v.y - event.rawY
                        downX = event.rawX
                        downY = event.rawY
                        isMoving = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = Math.abs(event.rawX - downX)
                        val dy = Math.abs(event.rawY - downY)
                        if (dx > touchSlop || dy > touchSlop) {
                            isMoving = true
                            var newX = event.rawX + dX
                            var newY = event.rawY + dY
                            newX = newX.coerceIn(minX, maxX)
                            newY = newY.coerceIn(minY, maxY)
                            v.x = newX
                            v.y = newY
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isMoving) {
                            // Hareket yoksa tıklama olarak algıla ve swap yap
                            viewModel.swapSurfaceViewRenderers()
                            return true
                        } else {
                            // Sürükleme bitti, kenara yaslama animasyonu
                            val midX = (maxX + minX) / 2
                            val targetX = if (v.x < midX) minX else maxX
                            val midY = (maxY + minY) / 2
                            val targetY = if (v.y < midY) minY else maxY
                            v.animate()
                                .x(targetX)
                                .y(targetY)
                                .setDuration(300)
                                .start()
                            return true
                        }
                    }
                }
                return false
            }
        })


    }

    override fun onDestroy() {
        Log.d("CallActivity", "onDestroy()")

        callId?.let {
            val status = viewModel.callStatus.value

            if (status != CallStatus.TIMEOUT && status != CallStatus.REJECTED && status != CallStatus.ENDED) {
                viewModel.endCall(it)
            }
        }

        super.onDestroy()
    }

    override fun onResume() {
        //Log.d("CallActivity", "onResume()")
        viewModel.startCameraIfItIsEnabledByUser()
        super.onResume()
    }

    override fun onStop() {
        //Log.d("CallActivity", "onStop()" )
        viewModel.stopCamera()
        super.onStop()
    }

    @Suppress("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        //super.onBackPressed()
        /*
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        */
    }

    private fun makeLocalRendererDraggable(view: SurfaceViewRenderer) {
        var dX = 0f
        var dY = 0f

        view.setOnTouchListener { v, event ->
            val parent = v.parent as ViewGroup

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    var newX = event.rawX + dX
                    var newY = event.rawY + dY

                    // Ekran dışına taşmayı engelle
                    val maxX = parent.width - v.width
                    val maxY = parent.height - v.height

                    newX = newX.coerceIn(0f, maxX.toFloat())
                    newY = newY.coerceIn(0f, maxY.toFloat())

                    v.x = newX
                    v.y = newY
                }
            }
            true
        }
    }

}