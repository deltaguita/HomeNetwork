package com.example.network

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.net.Proxy
import java.net.Socket
import javax.net.SocketFactory


class MainActivity : AppCompatActivity() {

    private lateinit var receiver: NetworkReceiver
    private lateinit var connMgr: ConnectivityManager
    private lateinit var wifiManager: WifiManager
    var wifiClient: OkHttpClient? = null
    var systemDefaultClient: OkHttpClient = OkHttpClient.Builder().build()
    var wifiService: StatusService? = null
    lateinit var serviceSystemDefault: StatusService

    private var isWifiEnable = false

    //Connect to public network
    private var isPublicNetworkConnected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        logcat.movementMethod = ScrollingMovementMethod()
        connMgr = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        setWifiStatus(wifiManager.isWifiEnabled)

        val retrofitSystemDefault = Retrofit.Builder().client(systemDefaultClient)
            .baseUrl("https://code-test.migoinc-dev.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        serviceSystemDefault = retrofitSystemDefault.create(StatusService::class.java)

        btn_call_api.setOnClickListener {

            when {
                isWifiEnable -> {
                    if (wifiService == null) {
                        initWifiService()
                    }
                    callPrivateAPI()
                }
                isPublicNetworkConnected -> {
                    callPublicAPI()
                }
                else -> {
                    logcat.append("There is no networks now, Please check network")
                }
            }

        }

        btn_clear_logcat.setOnClickListener {
            logcat.text = ""
        }


        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        receiver = NetworkReceiver()
        receiver.listener = object : NetworkReceiver.ConnectionChangeListener {
            override fun onConnectionChange() {
                logcat.append("--------Connection dedicated!!--------\n")
                setWifiStatus(wifiManager.isWifiEnabled)
                setCurrentNetworkInfo(connMgr.allNetworks)
            }

        }

        registerReceiver(receiver, filter)
    }

    private fun initWifiService() {
        val wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnectedOrConnecting) {
            val wifiNetwork = connMgr.allNetworks.firstOrNull { connMgr.getNetworkInfo(it)!!.type == ConnectivityManager.TYPE_WIFI }?.let {
                        wifiClient = OkHttpClient.Builder().proxy(Proxy.NO_PROXY)
                            .socketFactory(it.socketFactory).build()
                        val retrofitWifi = Retrofit.Builder().client(wifiClient!!)
                            .baseUrl("http://192.168.2.2/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        wifiService = retrofitWifi.create(StatusService::class.java)
                    }
        }


    }

    private fun callPrivateAPI() {
        logcat.append("Call private api...\n")
        wifiService?.getStatus()?.enqueue(object : retrofit2.Callback<Status> {
            override fun onFailure(call: retrofit2.Call<Status>, t: Throwable) {
                logcat.append("Call private api fail: ${t.message} \n")
                callPublicAPI()
            }

            override fun onResponse(
                call: retrofit2.Call<Status>,
                response: retrofit2.Response<Status>
            ) {
                logcat.append("Call private private success: ${response.code()} \n")
            }
        })
    }

    private fun callPublicAPI() {
        logcat.append("Call public api...\n")
        serviceSystemDefault.getStatus().enqueue(object : retrofit2.Callback<Status> {
            override fun onFailure(call: retrofit2.Call<Status>, t: Throwable) {
                logcat.append("Call public api fail: ${t.message} \n")
            }

            override fun onResponse(
                call: retrofit2.Call<Status>,
                response: retrofit2.Response<Status>
            ) {
                logcat.append("Call public api success: ${response.code()} \n")
            }
        })
    }

    public fun setWifiStatus(enable: Boolean) {
        isWifiEnable = enable
        logcat.append(
            if (enable) {
                "Wifi Current :Enable\n"
            } else {
                "Wifi Current :Disable\n"
            }
        )

    }

    public fun setCurrentNetworkInfo(networkWorks: Array<Network>) {
        logcat.append("目前的公用網路連線：\n")
        var newConnectionStatus = false
        networkWorks.forEach {
            val networkInfo = connMgr.getNetworkInfo(it)
            if (networkInfo != null && networkInfo.isConnected) {
                newConnectionStatus = true
                if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    logcat.append("Wifi:Connected\n")
                } else {
                    logcat.append("other:Connected\n")
                }
            }
        }
        isPublicNetworkConnected = newConnectionStatus
        if (!isPublicNetworkConnected) {
            logcat.append("無\n")
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}