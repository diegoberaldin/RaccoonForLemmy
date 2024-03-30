package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

interface NetworkManager {
    suspend fun isNetworkAvailable(): Boolean
}
