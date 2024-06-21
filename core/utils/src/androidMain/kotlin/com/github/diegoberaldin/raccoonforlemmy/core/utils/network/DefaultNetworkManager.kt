package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class DefaultNetworkManager(
    private val context: Context,
) : NetworkManager {
    @SuppressLint("MissingPermission")
    override suspend fun isNetworkAvailable(): Boolean =
        with(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) {
            // min SDK for project is 26 and methods used are 23+
            val netCapabilities = getNetworkCapabilities(activeNetwork)
            return netCapabilities != null && netCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && netCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
}
