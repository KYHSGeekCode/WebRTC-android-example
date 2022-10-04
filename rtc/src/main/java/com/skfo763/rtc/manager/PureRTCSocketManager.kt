package com.skfo763.rtc.manager


import android.util.Log
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.*

open class PureRTCSocketManager(private val onSocketListener: OnSocketListener) :
    PureWebsocketConnectManager(onSocketListener) {

    companion object {

        const val TYPE = "type"
        const val OFFER = "offer"
        const val ANSWER = "answer"
        const val CANDIDATE = "candidate"
        const val SDP = "sdp"

        const val EMIT_JOIN = "join"
        const val EMIT_MESSAGE = "message"
        const val EMIT_HANGUP = "hangup"

        const val ON_MATCHED = "matched"
        const val ON_WAITING_STATUS = "waiting_status"
        const val ON_TERMINATED = "terminated"

    }

    private val gson = Gson()

//    private val matchListener = Emitter.Listener {
//        onSocketListener.onSocketState(ON_MATCHED, it)
//    }
//
//    private val waitingStatusListener = Emitter.Listener {
//        onSocketListener.onSocketState(ON_WAITING_STATUS, it)
//    }
//
//    private val terminatedListener = Emitter.Listener {
//        onSocketListener.onSocketState(ON_TERMINATED, it)
//    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.w("PureRTCSocketManager", "onOpen")
        super.onOpen(webSocket, response)
    }

    fun socketJoin(data: JSONObject, ack: (String) -> Unit) {
        Log.w("socketJoin", data.toString())
        emit(EMIT_JOIN, data)

        // FIXME
//        {
//            try {
//                val ackJson = JSONObject("${it[it.size - 1]}")
//                if (ackJson.getBoolean("success")) {
//                    if (ackJson.getString("status") == FINISHED) {
//                        ack(FINISHED)
//                    }
//                } else {
//                    socketError(ackJson.getString("msg"))
//                }
//            } catch (e: SocketException) {
//                socketError(e.message ?: "socket exception")
//            } catch (e: JSONException) {
//                socketError(e.message ?: "json exception")
//            } catch (e: Exception) {
//                socketError(e.message ?: "exception")
//            }
//        }
    }

    fun sendOfferAnswerToSocket(sessionDescription: SessionDescription) {
        Log.w("sendOfferAnswerToSocket", sessionDescription.description)
        try {
            val jsonSessionDescription = gson.toJson(sessionDescription)
            val sendData = JSONObject()
            when {
                jsonSessionDescription.toLowerCase(Locale.ROOT).contains(OFFER) -> {
                    sendData.apply {
                        put(TYPE, OFFER)
                        put(SDP, sessionDescription.description)
                    }
                    emit(EMIT_MESSAGE, sendData)
                }
                jsonSessionDescription.toLowerCase(Locale.ROOT).contains(ANSWER) -> {
                    sendData.apply {
                        put(TYPE, ANSWER)
                        put(SDP, sessionDescription.description)
                    }
                    emit(EMIT_MESSAGE, sendData)
                }
                else -> {
                    socketError(message = "Invalid session description")
                }
            }
        } catch (e: Exception) {
            socketError(message = e.message ?: "sendOfferAnswerToSocket error")
        }
    }

    fun sendIceCandidateToSocket(iceCandidate: IceCandidate) {
        Log.w("sendIceCandidateToSocke", iceCandidate.sdp)
        try {
            val sendData = JSONObject().apply {
                put(TYPE, CANDIDATE)
                put(CANDIDATE, iceCandidate.sdp)
                put("id", iceCandidate.sdpMid)
                put("label", iceCandidate.sdpMLineIndex)
            }
            emit(EMIT_MESSAGE, sendData)
        } catch (e: Exception) {
            socketError(message = e.message ?: "sendIceCandidateToSocket error")
        }
    }

//    fun sendHangUpEventToSocket(
//        data: Any,
//        hangUp: (JSONObject) -> Unit,
//        hangUpSuccess: () -> Unit
//    ) {
//        try {
//            val jsonData = gson.toJson(data)
//            if (data == EMIT_HANGUP || jsonData == EMIT_HANGUP) {
//                emit(EMIT_HANGUP, JSONObject().put(EMIT_HANGUP, EMIT_HANGUP)) {
//                    val ackJson = JSONObject("${it[it.size - 1]}")
//                    hangUp(ackJson)
//                    if (ackJson.getBoolean("success")) {
//                        hangUpSuccess()
//                    } else {
//                        socketError(message = "hangup_false")
//                    }
//                }
//            } else {
//                socketError(message = "data should be hang-up : $data")
//            }
//        } catch (e: Exception) {
//            socketError(message = e.message ?: "sendHangUpEventToSocket error")
//        }
//    }

    fun sendEventToSocket(event: String) {
        Log.w("sendEventToSocket", event)
        emit(event, "")
    }

    fun socketError(message: String) {
        Log.w("socketError", message)
    }
}