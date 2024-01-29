package org.ohosdev.hapviewerandroid.ui.main

import android.app.Application
import android.net.Uri
import android.os.RemoteException
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.hutool.core.lang.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.deleteIfCache
import org.ohosdev.hapviewerandroid.extensions.destroy
import org.ohosdev.hapviewerandroid.extensions.getFileName
import org.ohosdev.hapviewerandroid.extensions.getOrCopyFile
import org.ohosdev.hapviewerandroid.extensions.init
import org.ohosdev.hapviewerandroid.extensions.installToSelf
import org.ohosdev.hapviewerandroid.extensions.installing
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.HapUtil
import org.ohosdev.hapviewerandroid.util.event.SnackBarEvent
import org.ohosdev.hapviewerandroid.util.helper.ShizukuServiceHelper
import java.io.File

// https://developer.android.google.cn/topic/libraries/architecture/viewmodel?hl=zh-cn
class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    val hapInfo: MutableLiveData<HapInfo> = MutableLiveData(HapInfo.INIT)
    val isParsing = MutableLiveData(false)
    val isInstalling = MutableLiveData(false)
    val snackBarEvent = MutableLiveData<SnackBarEvent>()
    private val isHapInfoInit get() = hapInfo.value!!.init

    private val shizukuServiceHelper = ShizukuServiceHelper()

    fun handelUri(uri: Uri) = viewModelScope.launch {
        isParsing.value = true
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
        isParsing.value = false
    }


    private suspend fun parseHap(file: File) = withContext(Dispatchers.Main) {
        isParsing.value = true
        withContext(Dispatchers.Default) {
            val destroyLastHapInfo = autoDestroyHapInfoRunnable()
            runCatching {
                updateHapInfo(HapUtil.parse(file.absolutePath))
            }.onFailure {
                it.printStackTrace()
                showSnackBar(R.string.parse_error_fail)
                file.deleteIfCache(app)
            }.onSuccess {
                destroyLastHapInfo()
            }
        }
        isParsing.value = false
    }


    fun showSnackBar(@StringRes resId: Int) {
        snackBarEvent.postValue(SnackBarEvent(app, resId))
    }

    fun showSnackBar(text: String) {
        snackBarEvent.postValue(SnackBarEvent(text))
    }

    fun installHapWaitingShizuku(hapInfo: HapInfo) {
        if (isHapInfoInit) return
        isInstalling.value = true
        runCatching {
            shizukuServiceHelper.bindUserService {
                viewModelScope.launch { installHap(hapInfo) }
            }
        }.onFailure {
            it.printStackTrace()
            showSnackBar(it.message ?: app.getString(R.string.unknown_error))
            isInstalling.value = false
            hapInfo.installing = false
        }
    }

    /**
     * 在UI线程内更新hapInfo
     * */
    private suspend fun updateHapInfo(newHapInfo: HapInfo) = withContext(Dispatchers.Main) {
        hapInfo.value = newHapInfo
    }

    @Throws(RemoteException::class)
    private suspend fun installHap(
        hapInfo: HapInfo = this@MainViewModel.hapInfo.value!!
    ) = withContext(Dispatchers.Main) {
        isInstalling.value = true
        hapInfo.installing = true
        withContext(Dispatchers.Default) {
            hapInfo.installToSelf(shizukuServiceHelper).also {
                // 不知为何无法显示结果
                if (it.isSuccess) {
                    showSnackBar(R.string.install_finished)
                } else {
                    showSnackBar(it.error ?: app.getString(R.string.unknown_error))
                }
            }
            autoDestroyHapInfoRunnable(hapInfo)()
        }
        isInstalling.value = false
        hapInfo.installing = false
    }

    /**
     * 仅当没有任何工作，且hap已变更时销毁
     *
     * 请确保在任何操作之后都调用该方法
     *
     * @param ignoreCurrentHapInfo 无论是否当前hapInfo一致，都允许销毁
     * */
    private fun autoDestroyHapInfoRunnable(
        hapInfo: HapInfo = this.hapInfo.value!!,
        ignoreCurrentHapInfo: Boolean = false
    ): () -> Unit {
        return fun() {
            if ((this.hapInfo.value != hapInfo || ignoreCurrentHapInfo)
                && !hapInfo.installing
                && !hapInfo.init
            ) {
                hapInfo.destroy(app)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching { shizukuServiceHelper.unbindUserService() }.onFailure { it.printStackTrace() }
        autoDestroyHapInfoRunnable(this.hapInfo.value!!, true)()
    }
}