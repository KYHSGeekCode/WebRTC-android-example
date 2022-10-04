package com.skfo763.rtc.manager

import android.content.Context
import android.util.Log
import com.skfo763.rtc.data.PEER_CREATE_ERROR
import com.skfo763.rtc.data.VIDEO_TRACK_ID
import com.skfo763.rtc.inobs.PeerConnectionObserver
import org.webrtc.*

class VideoPeerManager(
        private val context: Context,
        private val observer: PeerConnectionObserver
) : VoicePeerManager(context, observer) {

    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private val localVideoSource by lazy {
        peerConnectionFactory.createVideoSource(false)
    }

    private val localVideoTrack by lazy {
        peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource)
    }

    private var remoteVideoTrack: VideoTrack? = null

    private val videoCaptureManager = VideoCaptureManager.getVideoCapture(context, localVideoSource.capturerObserver)

    override fun PeerConnectionFactory.Builder.peerConnectionFactory(): PeerConnectionFactory.Builder {
        return this
    }

    override fun disconnectPeer() {
        localVideoSource.dispose()
        super.disconnectPeer()
    }

    fun initSurfaceView(surfaceViewRenderer: SurfaceViewRenderer) {
        surfaceViewRenderer.setEnableHardwareScaler(true)
        surfaceViewRenderer.init(rootEglBase.eglBaseContext, null)
    }

    fun startCameraCapture() {
        Log.w("VideoPeerManager", "Start Camera Capture")
        try {
            surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        } catch(e: Exception) {
            e.printStackTrace()
        } finally {
            surfaceTextureHelper?.let {
                videoCaptureManager.initialize(it, 960, 540, 30)
                localAudioTrack.setEnabled(true)
                localVideoTrack.setEnabled(true)
            }
        }
    }

    fun addTrackToStream() {
        Log.w("VideoPeerManager", "Add Track to Stream")
        localStream.addTrack(localAudioTrack)
        localStream.addTrack(localVideoTrack)
    }

    fun addStreamToPeerConnection() {
        Log.w("PeerManager", "Add stream to peer connection")
        val peerConnection = buildPeerConnection()
        this.peerConnection = peerConnection
        if(peerConnection == null) {
            observer.onPeerError(isCritical = true, showMessage = false, message = PEER_CREATE_ERROR)
            return
        }
        for (track in localStream.videoTracks) {
            peerConnection.addTrack(track)
        }
        for (track in localStream.audioTracks) {
            peerConnection.addTrack(track)
        }
//        peerConnection?.addStream(localStream) ?: run {
//            observer.
//        }
    }

    fun attachLocalTrackToSurface(localSurfaceView: SurfaceViewRenderer) {
        Log.w("PeerManager", "Attach local track to surface")
        localVideoTrack.addSink(localSurfaceView)
    }

    fun detachLocalTrackFromSurface(surfaceView: SurfaceViewRenderer) {
        Log.w("PeerManager", "Detach local track from surface")
        localVideoTrack.removeSink(surfaceView)
    }

    fun removeStreamFromPeerConnection() {
        Log.w("PeerManager", "Remove stream from peer connection")
        peerConnection?.removeStream(localStream) ?: run {
            observer.onPeerError(isCritical = false, showMessage = false, message = PEER_CREATE_ERROR)
        }
    }

    fun removeTrackFromStream() {
        Log.w("PeerManager", "Remove track from stream")
        localStream.removeTrack(localAudioTrack)
        localStream.removeTrack(localVideoTrack)
    }

    fun stopCameraCapture() {
        Log.w("PeerManager", "Stop camera capture")
        videoCaptureManager.stopVideoCapture {
            observer.onPeerError(isCritical = true, showMessage = false, message = PEER_CREATE_ERROR)
        }
        surfaceTextureHelper = null
    }

    fun changeCameraFacing(handler: CameraVideoCapturer.CameraSwitchHandler) {
        videoCaptureManager.changeCameraFacing(handler)
    }

    fun startRemoteVideoCapture(remoteSurfaceView: SurfaceViewRenderer, mediaStream: MediaStream) {
        Log.w("VideoPeerManager", "startRemoteVideoCapture")
        mediaStream.videoTracks.getOrNull(0)?.apply {
            remoteSurfaceView.setMirror(true)
            remoteVideoTrack = this
            addSink(remoteSurfaceView)
            setEnabled(true)
        } ?: run {
            observer.onPeerError(true, showMessage = false, message = PEER_CREATE_ERROR)
        }
    }

    fun stopRemotePreviewRendering(surfaceView: SurfaceViewRenderer) {
        remoteVideoTrack?.removeSink(surfaceView)
    }

}