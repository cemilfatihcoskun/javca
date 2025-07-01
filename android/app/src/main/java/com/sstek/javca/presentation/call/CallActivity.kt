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
import com.sstek.javca.databinding.ActivityCallBinding
import com.sstek.javca.domain.model.CallStatus
import com.sstek.javca.presentation.call.CallViewModel
import com.sstek.javca.presentation.main.MainActivity
import com.sstek.javca.presentation.register.RegisterActivity
import org.webrtc.SurfaceViewRenderer

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {
    private val viewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        viewModel.callStatus.observe(this) { status ->
            if (status == CallStatus.REJECTED || status == CallStatus.TIMEOUT) {
                Log.d("CallActivity", "rejected or timeout")
                finish()
            }

            if (status == CallStatus.ENDED) {
                Log.d("CallActivity", "ended")
                viewModel.endCall(callId!!)
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                })
                finish()
            }

        }

        viewModel.initWebRtcManager(callId.toString(), this, localRenderer, remoteRenderer)


        if (isCaller) {
            viewModel.createOffer()
        }

        /*
        binding.toggleAudioButton.setOnClickListener {
        }
         */

        binding.toggleVideoButton.setOnClickListener {
            viewModel.toggleVideo()
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
