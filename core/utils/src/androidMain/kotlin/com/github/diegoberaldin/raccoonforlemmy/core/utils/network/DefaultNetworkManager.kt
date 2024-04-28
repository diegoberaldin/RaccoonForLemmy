package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class DefaultNetworkManager(
    private val context: Context,
) : NetworkManager {
    @SuppressLint("MissingPermission")
    override suspend fun isNetworkAvailable(): Boolean =
        with(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities = getNetworkCapabilities(activeNetwork) ?: return@with false
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            } else if (activeNetworkInfo != null && activeNetworkInfo?.isConnectedOrConnecting == true) {
                true
            } else {
                false
            }
        }
}
