package org.ohosdev.hapviewerandroid.ui.main

import android.app.Application
import android.net.Uri
import android.os.RemoteException
import android.provider.DocumentsContract
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.hutool.core.lang.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.canRead
import org.ohosdev.hapviewerandroid.extensions.deleteIfCache
import org.ohosdev.hapviewerandroid.extensions.destroy
import org.ohosdev.hapviewerandroid.extensions.getOrCopyFile
import org.ohosdev.hapviewerandroid.extensions.installToSelf
import org.ohosdev.hapviewerandroid.extensions.toDocumentFile
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.event.SnackBarEvent
import org.ohosdev.hapviewerandroid.util.helper.ShizukuServiceHelper
import org.ohosdev.hapviewerandroid.util.ohos.HapUtil
import java.io.File

// https://developer.android.google.cn/topic/libraries/architecture/viewmodel?hl=zh-cn
class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    val hapInfo: MutableLiveData<HapInfo> = MutableLiveData(HapInfo.INIT)
    val isParsing = MutableLiveData(false)
    val isInstalling = MutableLiveData(false)
    val snackBarEvent = MutableLiveData<SnackBarEvent>()
    private val isHapInfoInit get() = hapInfo.value!!.isInit

    private val shizukuServiceHelper = ShizukuServiceHelper()

    fun handelHapUri(uri: Uri) = viewModelScope.launch(Dispatchers.Main) {
        Log.i(TAG, "handelUri: $uri")
        isParsing.value = true
        val destroyLastHapInfo = autoDestroyHapInfoRunnable()
        val isSuccess = run {
            val file = obtainFile(uri) ?: return@run false
            val hapInfo = parseHap(file)
            if (hapInfo == null) {
                file.deleteIfCache(app)
                return@run false
            }
            this@MainViewModel.hapInfo.value = hapInfo
            return@run true
        }
        if (isSuccess) {
            destroyLastHapInfo()
        }
        isParsing.value = false
    }

    private suspend fun obtainFile(uri: Uri) = withContext(Dispatchers.IO) {
        val documentFile = uri.toDocumentFile(app) ?: run {
            showSnackBar(R.string.error_uri_not_valid)
            return@withContext null
        }
        // 第三方的实现可能有问题，所以仅 DocumentUri 需要判断uri。
        DocumentsContract.isDocumentUri(app, uri).also {
            if (it && !documentFile.isFile) {
                showSnackBar(R.string.error_uri_not_document)
                return@withContext null
            }
            if (it && !uri.canRead(app)) {
                showSnackBar(R.string.error_file_unreadable)
                return@withContext null
            }
        }

        // TODO: 文件名校验
        val fileName = documentFile.name ?: "unknown"
        val randomName = "${UUID.randomUUID().toString(true)}_$fileName"
        documentFile.getOrCopyFile(app, randomName) ?: run {
            showSnackBar(R.string.parse_error_fail_obtain)
            return@withContext null
        }
    }

    private suspend fun parseHap(file: File): HapInfo? = withContext(Dispatchers.Default) {
        runCatching {
            HapUtil.parse(file.absolutePath)
        }.onFailure {
            it.printStackTrace()
            showSnackBar(R.string.parse_error_fail)
        }.getOrNull()
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
                installHap(hapInfo)
            }
        }.onFailure {
            it.printStackTrace()
            showSnackBar(it.message ?: app.getString(R.string.unknown_error))
            isInstalling.value = false
            hapInfo.isInstalling = false
        }
    }

    @Throws(RemoteException::class)
    private fun installHap(hapInfo: HapInfo) = viewModelScope.launch(Dispatchers.Main) {
        hapInfo.isPreinstallation = true
        isInstalling.value = true
        hapInfo.installToSelf(shizukuServiceHelper).also {
            // 不知为何无法显示真正的结果
            if (it.isSuccess) {
                showSnackBar(R.string.install_finished)
            } else {
                showSnackBar(it.error ?: app.getString(R.string.unknown_error))
            }
        }
        withContext(Dispatchers.Default) { autoDestroyHapInfoRunnable(hapInfo)() }
        hapInfo.isPreinstallation = false
        isInstalling.value = false
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
                && !hapInfo.isPreinstallation
                && !hapInfo.isInstalling
                && !hapInfo.isInit
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

    companion object {
        private const val TAG = "MainViewModel"
    }
}