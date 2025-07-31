package com.sstek.javca.framework

import android.content.Context
import android.util.Log
import com.sstek.javca.core.config.Config
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class WebRtcManager(
    private val context: Context,
    private val callId: String,
    private val onIceCandidate: (IceCandidate) -> Unit,
    private val onSdpCreated: (SessionDescription) -> Unit,
    private val onCallEnded: () -> Unit
) {
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localSurfaceView: SurfaceViewRenderer? = null
    private var remoteSurfaceView: SurfaceViewRenderer? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private lateinit var eglBase: EglBase

    private var isSurfacesSwapped: Boolean = false
    private var isClosed: Boolean = false

    private val VIDEO_TRACK_CHANNEL = "100"
    private val AUDIO_TRACK_CHANNEL = "101"

    private fun initSurfaceView(view: SurfaceViewRenderer, eglContext: EglBase.Context) {
        view.init(eglContext, null)
        view.setEnableHardwareScaler(true)
    }

    fun init(localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        eglBase = EglBase.create()

        initSurfaceView(localView, eglBase.eglBaseContext)
        initSurfaceView(remoteView, eglBase.eglBaseContext)



        localSurfaceView = localView
        remoteSurfaceView = remoteView

        localSurfaceView?.setZOrderMediaOverlay(true)
        remoteSurfaceView?.setZOrderMediaOverlay(false)


        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val encoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext, true, true
        )
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                this@WebRtcManager.onIceCandidate(candidate)
                Log.d("WebRtcManager", "onIceCandidate()")
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
                Log.d("WebRtcManager", "onIceCandidatesRemoved()")
            }

            override fun onAddStream(stream: MediaStream?) {
                Log.d("WebRtcManager", "onAddStream()")
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                Log.d("WebRtcManager", "onIceConnectionChange() $newState")
                // TODO(10 saniye bekleme mekanizması ekle)
                if (newState == PeerConnection.IceConnectionState.CLOSED || newState == PeerConnection.IceConnectionState.DISCONNECTED || newState == PeerConnection.IceConnectionState.FAILED) {
                    onCallEnded()
                }

            }

            override fun onDataChannel(dc: DataChannel?) {
                Log.d("WebRtcManager", "onDataChannel()")
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
                Log.d("WebRtcManager", "onIceConnectionReceivingChange()")
            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                Log.d("WebRtcManager", "onIceGatheringChange()")
            }

            override fun onRemoveStream(stream: MediaStream?) {
                Log.d("WebRtcManager", "onRemoveStream()")
            }

            override fun onRenegotiationNeeded() {
                Log.d("WebRtcManager", "onRenegotiationNeeded()")
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                Log.d("WebRtcManager", "onSignalingChange()")
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                //TODO(Audio Track ya da başka bir video track sıkıntı oluşturabilir)
                Log.d("WebRtcManager", "onTrack()")

                val track = transceiver?.receiver?.track()
                if (track is VideoTrack) {
                    remoteVideoTrack = track
                    remoteVideoTrack?.addSink(remoteSurfaceView)
                    Log.d("WebRtcManager", "Video track added to remote view")
                } else if (track is AudioTrack) {
                    Log.d("WebRtcManager", "Audio track added.")
                } else {
                    Log.d("WebRtcManager", "Received non-video and non-audio track")
                }
            }
        })

        videoCapturer = createCameraCapturer()
        val videoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturer?.initialize(
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext),
            context,
            videoSource.capturerObserver
        )
        videoCapturer?.startCapture(Config.CAMERA_WIDTH, Config.CAMERA_HEIGHT, Config.CAMERA_FPS)

        localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_CHANNEL, videoSource)
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_CHANNEL, peerConnectionFactory.createAudioSource(MediaConstraints()))

        peerConnection?.addTrack(localVideoTrack, listOf("stream1"))
        peerConnection?.addTrack(localAudioTrack, listOf("stream1"))

        localSurfaceView?.post {
            localVideoTrack?.addSink(localSurfaceView)
            Log.d("WebRtcManager", "Local video track -> localSurfaceView")
        }
    }

    fun setRemoteDescription(sdp: SessionDescription, onSetSuccess: (() -> Unit)? = null) {
        peerConnection?.setRemoteDescription(object : SdpObserverAdapter() {
            override fun onSetSuccess() {
                onSetSuccess?.invoke()
            }
        }, sdp)
    }

    fun addRemoteIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun createOffer() {
        peerConnection?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp != null) {
                    peerConnection?.setLocalDescription(object : SdpObserverAdapter() {}, sdp)
                    this@WebRtcManager.onSdpCreated(sdp)
                }
            }
        }, MediaConstraints())
    }

    fun createAnswer() {
        peerConnection?.createAnswer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp != null) {
                    peerConnection?.setLocalDescription(object : SdpObserverAdapter() {}, sdp)
                    this@WebRtcManager.onSdpCreated(sdp)
                }
            }
        }, MediaConstraints())
    }

    fun close() {
        if (isClosed) {
            return
        }
        try {
            peerConnection?.close()
            peerConnection = null

            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null

            localVideoTrack?.setEnabled(false)
            localAudioTrack?.setEnabled(false)
            remoteVideoTrack?.setEnabled(false)

            localVideoTrack?.removeSink(localSurfaceView)
            remoteVideoTrack?.removeSink(remoteSurfaceView)

            localVideoTrack = null
            localAudioTrack = null
            remoteVideoTrack = null

            localSurfaceView?.release()
            localSurfaceView = null

            remoteSurfaceView?.release()
            remoteSurfaceView = null

            eglBase.release()
        } catch (e: Exception) {
            Log.e("WebRtcManager", "${e.message}")
        }
        isClosed = true
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        return null
    }

    fun toggleMicrophone(): Boolean {
        localAudioTrack?.let {
            val reverse = !it.enabled()
            it.setEnabled(reverse)
            return reverse
        }
        return false
    }

    fun toggleVideo(): Boolean {
        localVideoTrack?.let {
            val reverse = !it.enabled()
            it.setEnabled(reverse)
            return reverse
        }
        return false
    }

    fun getVideoStatus(): Boolean {
        localVideoTrack?.let {
            return it.enabled()
        }
        return false
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    fun startCamera() {
        localVideoTrack?.let {
            it.setEnabled(true)
        }
    }

    fun stopCamera() {
        localVideoTrack?.let {
            it.setEnabled(false)
        }
    }

    fun swapSurfaceViewRenderers() {
        if (localSurfaceView == null || remoteSurfaceView == null) {
            return
        }

        if (!isSurfacesSwapped) {
            localVideoTrack?.removeSink(localSurfaceView)
            remoteVideoTrack?.removeSink(remoteSurfaceView)

            localSurfaceView?.post {
                remoteVideoTrack?.addSink(localSurfaceView)
            }

            remoteSurfaceView?.post {
                localVideoTrack?.addSink(remoteSurfaceView)
            }
        } else {
            remoteVideoTrack?.removeSink(localSurfaceView)
            localVideoTrack?.removeSink(remoteSurfaceView)

            localSurfaceView?.post {
                localVideoTrack?.addSink(localSurfaceView)
            }

            remoteSurfaceView?.post {
                remoteVideoTrack?.addSink(remoteSurfaceView)
            }
        }

        isSurfacesSwapped = !isSurfacesSwapped
    }
}
