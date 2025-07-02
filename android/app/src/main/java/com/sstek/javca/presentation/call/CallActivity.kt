package com.sstek.javca.presentation.call

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

import com.sstek.javca.databinding.ActivityLoginBinding
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sstek.javca.Config
import com.sstek.javca.databinding.ActivityCallBinding
import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.presentation.call.CallViewModel
import com.sstek.javca.presentation.main.MainActivity
import com.sstek.javca.presentation.register.RegisterActivity
import org.webrtc.SurfaceViewRenderer

import com.sstek.javca.R

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {
    private val viewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding

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
        val callId = intent.getStringExtra("callId")

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
                    Snackbar.make(binding.root, "Aranıyor.", Snackbar.LENGTH_LONG).show()
                }
                CallStatus.ACCEPTED -> {

                }
                CallStatus.REJECTED -> {
                    Log.d("CallActivity", "Rejected")

                    val snackbar = Snackbar.make(binding.root, "Arama reddedildi", Snackbar.LENGTH_LONG)
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

                    val snackbar = Snackbar.make(binding.root, "Aradığınız kişiye ulaşılamadı.", Snackbar.LENGTH_LONG)
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

                    val snackbar = Snackbar.make(binding.root, "Arama bitti.", Snackbar.LENGTH_LONG)
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

        /*
        binding.toggleAudioButton.setOnClickListener {
        }
         */

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
}
