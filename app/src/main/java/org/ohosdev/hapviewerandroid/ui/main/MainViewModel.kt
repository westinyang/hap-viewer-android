package org.ohosdev.hapviewerandroid.ui.main

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.hutool.core.lang.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.deleteIfCache
import org.ohosdev.hapviewerandroid.extensions.destroy
import org.ohosdev.hapviewerandroid.extensions.getFileName
import org.ohosdev.hapviewerandroid.extensions.getOrCopyFile
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.HapUtil
import org.ohosdev.hapviewerandroid.util.event.SnackBarEvent
import org.ohosdev.hapviewerandroid.util.helper.ShizukuServiceHelper
import java.io.File

// https://developer.android.google.cn/topic/libraries/architecture/viewmodel?hl=zh-cn
class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    val hapInfo: MutableLiveData<HapInfo> by lazy {
        MutableLiveData(HapInfo.INIT)
    }
    val isParsing = MutableLiveData(false)
    val isInstalling = MutableLiveData(false)
    val snackBarEvent = MutableLiveData<SnackBarEvent>()
    val isHapInfoInit get() = hapInfo.value!!.init

    private val shizukuServiceHelper = ShizukuServiceHelper()

    fun handelUri(uri: Uri) {
        viewModelScope.launch {
            isParsing.postValue(true)
            withContext(Dispatchers.IO) {
                // TODO: 文件名校验
                val uuid = UUID.randomUUID().toString(true)
                val fileName = uri.getFileName(app)
                val name = "${uuid}_$fileName"

                val file = uri.getOrCopyFile(app, name)
                if (file == null) {
                    showSnackBar(R.string.parse_error_fail_obtain)
                    return@withContext
                }
                parseHap(file)
            }
            isParsing.postValue(false)
        }
    }

    private suspend fun parseHap(file: File) {
        isParsing.postValue(true)
        coroutineScope {
            try {
                val destroyRunnable = autoDestroyHapInfoRunnable(hapInfo.value!!)
                this@MainViewModel.hapInfo.postValue(HapUtil.parse(file.absolutePath))
                destroyRunnable.run()
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackBar(R.string.parse_error_fail)
                file.deleteIfCache(app)
            }
        }
        isParsing.postValue(false)
    }

    fun showSnackBar(@StringRes resId: Int) {
        snackBarEvent.postValue(SnackBarEvent(app, resId))
    }

    fun showSnackBar(text: String) {
        snackBarEvent.postValue(SnackBarEvent(text))
    }

    fun installHapWaitingShizuku(hapInfo: HapInfo) {
        if (isHapInfoInit) return
        isInstalling.postValue(true)
        runCatching {
            shizukuServiceHelper.bindUserService {
                installHap(hapInfo)
            }
        }.onFailure {
            isInstalling.postValue(false)
            showSnackBar(it.message ?: "Unknown error")
        }
    }

    private fun installHap(hapInfo: HapInfo) {
        isInstalling.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                runCatching {
                    HapUtil.installHap(
                        shizukuServiceHelper, this@MainViewModel.hapInfo.value!!.hapFilePath
                    )
                    // 不知为何无法显示结果
                    showSnackBar(R.string.install_finished)
                }.onFailure {
                    showSnackBar(it.message ?: app.getString(R.string.unknown_error))
                }
            }
            isInstalling.postValue(false)
            autoDestroyHapInfoRunnable(hapInfo).run()
        }
    }

    /**
     * 仅当没有任何工作，且hap已变更时销毁
     * */
    private fun autoDestroyHapInfoRunnable(hapInfo: HapInfo?): Runnable {
        return Runnable {
            if (this.hapInfo.value != hapInfo
                && isInstalling.value != true
                && hapInfo != null
            ) {
                hapInfo.destroy(app)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        hapInfo.value?.destroy(app)
    }
}