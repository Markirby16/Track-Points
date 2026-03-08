package com.example.trackpoints.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NetworkConnectivityService(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private suspend fun isInternetActuallyAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            val activeNetwork = connectivityManager.activeNetwork ?: return@withContext false

            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true) {
                return@withContext true
            }

            try {
                val url = URL("https://clients3.google.com/generate_204")
                val connection = activeNetwork.openConnection(url) as HttpURLConnection
                connection.connectTimeout = 2000  // 2-second timeout
                connection.readTimeout = 2000
                connection.requestMethod = "GET"
                connection.connect()

                connection.responseCode == 204
            } catch (e: IOException) {
                false
            }
        }
    }

    fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        launch {
            send(isInternetActuallyAvailable())
        }

        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                launch { send(isInternetActuallyAvailable()) }
            }

            override fun onLost(network: Network) {
                launch { send(isInternetActuallyAvailable()) }
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                launch { send(isInternetActuallyAvailable()) }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()
}
