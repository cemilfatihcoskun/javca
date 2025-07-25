package com.sstek.javca.call.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

                    val snackbar = Snackbar.make(binding.root, "Arama reddedildi", Snackbar.LENGTH_SHORT)
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

                    val snackbar = Snackbar.make(binding.root, "Aradığınız kişiye ulaşılamadı.", Snackbar.LENGTH_SHORT)
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

                    val snackbar = Snackbar.make(binding.root, "Arama bitti.", Snackbar.LENGTH_SHORT)
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            viewModel.endCall(callId!!)
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

        binding.localVideoSurfaceViewRenderer.setOnClickListener {
            viewModel.swapSurfaceViewRenderers()
        }
    }

    override fun onDestroy() {
        Log.d("CallActivity", "onDestroy()")
        callId?.let {
            Log.d("CallActivity", "hey $callId")
            viewModel.endCall(it)
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
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}