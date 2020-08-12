package com.example.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NetworkReceiver : BroadcastReceiver() {

    var listener: ConnectionChangeListener? = null

    interface ConnectionChangeListener {
        fun onConnectionChange()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (listener != null) {
            listener!!.onConnectionChange()
        }
    }


}