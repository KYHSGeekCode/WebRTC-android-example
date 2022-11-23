package com.skfo763.rtc.manager

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

open class PureWebsocketConnectManager(private val onSocketListener: OnSocketListener) :
    WebSocketListener() {
    protected var socket: WebSocket? = null
    private val retryConnectionCount = AtomicInteger(0)

    companion object {
        const val RECONNECTION = true                    // 재연결 시도 여부
        const val RANDOMIZATION_FACTOR = 1.0             // 지연시간 오차 범위
        const val RECONNECTION_ATTEMPTS = 10             // 재연결 시도 횟수
        const val RECONNECTION_DELAY = 1000L             // 재연결 시도 시간
        const val RECONNECTION_DELAY_MAX = 1000L         // 최대 재연결 시도 시간
    }

    // 로컬 테스트 시 ssl 인증 우회하기 위해 필요합니다. 자체 ssl 인증 서버가 있으면 안써도 됩니다.
    private fun getOkHttpClient(): OkHttpClient {
        Log.w("PureWebsocketConnect", "getOkHttpClient")
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, null)
        }
        val trustManager = trustAllCerts[0] as X509TrustManager
        return OkHttpClient.Builder()
            .hostnameVerifier { _, _ -> true }
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .build()
    }


    fun createSocket(host: String) {
        Log.w("PureWebsocketConnect", "createSocket")
        if (socket != null) return
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("ws://10.0.2.2:8080/chatt")
            .build()

        socket = client.newWebSocket(request, this)
        client.dispatcher().executorService().shutdown()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.w("PureWebsocketConnect", "onOpen")
        retryConnectionCount.set(0)
        onSocketListener.onSocketState("connect", arrayOf())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.w("PureWebsocketConnect", "onClosing")
        onSocketListener.onSocketState("disconnect", arrayOf())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.w("socketmgr", "onMessage: $text")
        val json = JSONObject(text)
        val event = json.getString("event")
        val data = json.getString("content")
        onSocketListener.onSocketState(event, arrayOf(data))
    }

    // reconnect
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.e("socketmgr", "onFailure: ${t.message}, ${response?.message()}")
        val connectCount = retryConnectionCount.get()
        if (connectCount >= RECONNECTION_ATTEMPTS - 1) {
            onSocketListener.onSocketState("connectError", emptyArray())
            retryConnectionCount.set(0)
        } else if (connectCount % 5 == 0) {
            onSocketListener.onSocketState("connectRetryError", emptyArray())
        }
        Log.w("webrtcTAG", "socket retry event, retry count = $connectCount")
        retryConnectionCount.set(connectCount + 1)
    }


//    private val reconnectListener = Emitter.Listener {
//        onSocketListener.onSocketState("reconnect", it)
//    }

    fun disconnectSocket() {
        Log.w("PureWebsocketConnect", "disconnectSocket")
        socket?.close(1000, "disconnect") // TODO: Fix args
        socket = null
    }

    fun emit(event: String, data: Any) {
        Log.w("socketmgr", "emit: $event, $data, socket: $socket, selfId: $selfId")
        socket?.send(
            JSONObject().apply {
                put("traceId", UUID.randomUUID().toString())
                put("from", selfId)
                put("event", event)
                put("content", data)
            }.toString()
        )
    }

    // 없다 생각해야 함
//    fun emit(event: String, vararg data: Any, onCall: ((args: Array<Any?>) -> Unit)? = null) {
//        if (onCall == null) {
//            socket?.emit(event, data)
//        } else {
//            socket?.emit(event, data) { onCall(it) }
//        }
//    }
}
val selfId = UUID.randomUUID().toString()