package com.github.shingyx.wakeonlan.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.shingyx.wakeonlan.R
import com.github.shingyx.wakeonlan.data.Host
import com.github.shingyx.wakeonlan.data.MagicPacketProcessor
import com.github.shingyx.wakeonlan.data.Storage

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = Storage.getInstance(application)
    private val magicPacketProcessor = MagicPacketProcessor(application)

    private val _host = MutableLiveData<Host?>()
    val host: LiveData<Host?> = _host

    private val _turnOnResult = MutableLiveData<Result<Unit>>()
    val turnOnResult: LiveData<Result<Unit>> = _turnOnResult

    private val _hostScanResult = MutableLiveData<List<Host>>()
    val hostScanResult: LiveData<List<Host>> = _hostScanResult

    init {
        _host.value = storage.savedHost
    }

    suspend fun turnOn() {
        val result = Result.runCatching {
            val host = storage.savedHost
                ?: throw Exception(getApplication<Application>().getString(R.string.error_null_host))
            magicPacketProcessor.send(host)
        }
        _turnOnResult.value = result
    }

    suspend fun scanForHosts() {
        val hosts = magicPacketProcessor.scanForHosts()
        _hostScanResult.value = hosts
    }

    fun selectHost(host: Host) {
        storage.savedHost = host
        _host.value = host
    }
}
