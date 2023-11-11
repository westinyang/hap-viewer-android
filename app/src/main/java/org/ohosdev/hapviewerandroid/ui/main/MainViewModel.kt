package org.ohosdev.hapviewerandroid.ui.main

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.getOrCopyFile
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.HapUtil
import org.ohosdev.hapviewerandroid.util.event.SnackBarEvent
import java.io.File

// https://developer.android.google.cn/topic/libraries/architecture/viewmodel?hl=zh-cn
class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    val hapInfo: MutableLiveData<HapInfo> by lazy {
        MutableLiveData(HapInfo(true))
    }
    val isParsing = MutableLiveData(false)
    val snackBarEvent = MutableLiveData<SnackBarEvent>()

    fun handelUri(uri: Uri) {
        viewModelScope.launch {
            isParsing.postValue(true)
            withContext(Dispatchers.IO) {
                // TODO: 文件名校验
                val file = uri.getOrCopyFile(app)
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
                this@MainViewModel.hapInfo.postValue(HapUtil.parse(file.absolutePath))
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackBar(R.string.parse_error_fail)
            }
            // 到此为止，这个临时文件没用了，可以删掉了
            // file.deleteIfCache(app)
        }
        isParsing.postValue(false)
    }

    fun showSnackBar(@StringRes resId: Int) {
        snackBarEvent.postValue(SnackBarEvent(app, resId))
    }

    override fun onCleared() {
        super.onCleared()
        val hapInfoValue = hapInfo.value
        hapInfoValue?.icon?.recycle()
    }
}