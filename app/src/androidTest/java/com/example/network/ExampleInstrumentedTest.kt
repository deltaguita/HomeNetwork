package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.notify
import okhttp3.internal.wait
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


/**
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {


    @Test
    fun getNetworkStatus() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val connMgr =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isWifiConn: Boolean = false
        var isMobileConn: Boolean = false
        //應該實作這個來取得網路狀態的變化
//        connMgr.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
//
//        })
        //測試先假定有 wifi lol~
//        val wifiNetwork: Network? =
//            connMgr.allNetworks.firstOrNull { connMgr.getNetworkInfo(it).type == ConnectivityManager.TYPE_WIFI }
//        val client = OkHttpClient.Builder()
//            .socketFactory(wifiNetwork!!.socketFactory)
//            .build();
//        val request = Request.Builder().url("https://github.com/square/okhttp").build()
//        val response = client.newCall(request).execute()

        connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)


//        Assert.assertEquals(response.code,200)

    }


    @Test
    fun testNetWorkCallback(){

        val syncObject = Any()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val connMgr =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val connectivityManagerCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {

            private val activeNetworks: MutableList<Network> = mutableListOf()



            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                // Add to list of active networks if not already in list
                if (activeNetworks.none { activeNetwork -> activeNetwork.networkHandle == network.networkHandle }) activeNetworks.add(network)
                connMgr.getNetworkInfo(network)
                val isNetworkConnected = activeNetworks.isNotEmpty()

                // 結論： connectivityManagerCallback 會給當下最好的 network

            }

            override fun onLost(network: Network) {
                super.onLost(network)

                // Remove network from active network list
                activeNetworks.removeAll { activeNetwork -> activeNetwork.networkHandle == network.networkHandle }
                val isNetworkConnected = activeNetworks.isNotEmpty()
                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        }
//        assertEquals("com.example.network", appContext.packageName)

        connMgr.registerNetworkCallback(NetworkRequest.Builder().build(),connectivityManagerCallback)
        synchronized (syncObject){
            syncObject.wait();
        }
    }

}