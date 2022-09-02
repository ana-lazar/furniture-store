package com.ma.ikea.data.remote

import com.ma.ikea.logd
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.*

object ProductDataSource {
    val eventChannel = Channel<String>()
    private var webSocket: WebSocket? = null
    private val request = Request.Builder().url("ws://192.168.0.73:3000").build()

    fun createWebSocket() {
        webSocket = OkHttpClient().newWebSocket(
            request,
            MyWebSocketListener()
        )
    }

    fun destroyWebSocket() {
        webSocket?.close(1000, null)
    }

    private class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            logd("onOpen - WebSocket")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            logd("onMessage - WebSocket received message $text")
            runBlocking { eventChannel.send(text) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            logd("onClosing - WebSocket")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            logd("onClosed - WebSocket")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            logd("onFailure - WebSocket $t")
        }
    }
}