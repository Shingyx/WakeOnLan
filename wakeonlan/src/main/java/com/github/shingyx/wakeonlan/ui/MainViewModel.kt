package com.github.shingyx.wakeonlan.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.shingyx.wakeonlan.R
import com.github.shingyx.wakeonlan.data.Host
import com.github.shingyx.wakeonlan.data.Storage
import com.github.shingyx.wakeonlan.data.NetworkHandler

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = Storage.getInstance(application)
    private val networkHandler = NetworkHandler(application)

    private val _host = MutableLiveData<Host?>()
    val host: LiveData<Host?> = _host

    private val _turnOnPcResult = MutableLiveData<Result<Unit>>()
    val turnOnPcResult: LiveData<Result<Unit>> = _turnOnPcResult

    init {
        _host.value = storage.savedHost
    }

    suspend fun turnOnPc() {
        val result = runCatching {
            val host = storage.savedHost
                ?: throw Exception(getApplication<Application>().getString(R.string.error_null_host))
            networkHandler.sendMagicPacket(host)
        }
        _turnOnPcResult.value = result
    }

    fun selectHost(host: Host) {
        storage.savedHost = host
        _host.value = host
    }
}
