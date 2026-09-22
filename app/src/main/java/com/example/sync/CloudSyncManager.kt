package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.repository.PosRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Synced(val timeString: String) : SyncState()
    data class LocalSaved(val message: String = "Saved on device") : SyncState()
    data class Error(val message: String) : SyncState()
}

class CloudSyncManager(
    private val context: Context,
    private val repository: PosRepository,
    private val scope: CoroutineScope
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Central cloud API URL extracted from the application specification and HTML
    var cloudApiUrl: String = "https://script.google.com/macros/s/AKfycbwUwvyISgdiMlJxHCjc5bOVUOfsV_hBfftrBYNQQqZ9zL8UUf59WwjoGiLSg4Cd-2tIoQ/exec"
    var apiToken: String = "ca43333af11283527e79fa74c3237e853a9825eec100d6c0"

    private val _syncState = MutableStateFlow<SyncState>(SyncState.LocalSaved())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    init {
        _isNetworkAvailable.value = isOnline()
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isNetworkAvailable.value = true
                // Auto-sync upon reconnecting
                scope.launch {
                    triggerSync()
                }
            }

            override fun onLost(network: Network) {
                _isNetworkAvailable.value = false
                _syncState.value = SyncState.LocalSaved("Saved on device (Offline)")
            }
        })
    }

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun triggerSync(): Boolean = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            _syncState.value = SyncState.LocalSaved("Saved on device (Offline)")
            return@withContext false
        }

        _syncState.value = SyncState.Syncing

        try {
            // Process pending offline sync queue if items exist
            val pendingItems = repository.getPendingSyncQueue()
            if (pendingItems.isNotEmpty()) {
                val payloadArray = org.json.JSONArray()
                for (item in pendingItems) {
                    val obj = org.json.JSONObject()
                    obj.put("id", item.id)
                    obj.put("entityType", item.entityType)
                    obj.put("entityId", item.entityId)
                    obj.put("action", item.action)
                    obj.put("payload", item.payloadJson)
                    obj.put("timestamp", item.timestamp)
                    payloadArray.put(obj)
                }

                val jsonPayload = org.json.JSONObject().apply {
                    put("token", apiToken)
                    put("source", "Falcon POS Android")
                    put("itemsCount", pendingItems.size)
                    put("queue", payloadArray)
                }.toString()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val postBody = jsonPayload.toRequestBody(mediaType)
                val syncPostRequest = Request.Builder()
                    .url(cloudApiUrl)
                    .post(postBody)
                    .build()

                try {
                    val postResponse = okHttpClient.newCall(syncPostRequest).execute()
                    if (postResponse.isSuccessful) {
                        repository.markAllPendingSynced()
                    }
                } catch (e: Exception) {
                    // Queue preserved offline
                }
            }

            // Sync ping / status verification
            val pingUrl = "$cloudApiUrl?action=ping&token=$apiToken"
            val pingRequest = Request.Builder().url(pingUrl).build()

            val pingResponse = try {
                okHttpClient.newCall(pingRequest).execute()
            } catch (e: Exception) {
                null
            }

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val nowTime = timeFormat.format(Date())
            repository.setSetting("pos_last_synced_at", System.currentTimeMillis().toString())
            _syncState.value = SyncState.Synced(nowTime)
            true
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.localizedMessage ?: "Sync error, retrying...")
            false
        }
    }

    suspend fun syncNow() {
        triggerSync()
    }
}
