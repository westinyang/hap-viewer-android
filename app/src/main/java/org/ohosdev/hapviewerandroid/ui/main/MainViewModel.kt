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
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.HapUtil
import org.ohosdev.hapviewerandroid.util.MyFileUtil
import org.ohosdev.hapviewerandroid.util.event.SnackBarEvent

// https://developer.android.google.cn/topic/libraries/architecture/viewmodel?hl=zh-cn
class MainViewModel(private val application: Application) : AndroidViewModel(application) {

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
                val file = MyFileUtil.getOrCopyFile(application, uri)
                if (file == null) {
                    showSnackBar(R.string.parse_error_fail_obtain)
                    return@withContext
                }
                parseHap(file.absolutePath)
            }
            isParsing.postValue(false)
        }
    }

    private suspend fun parseHap(filePath: String) {
        isParsing.postValue(true)
        coroutineScope {
            val hapInfo: HapInfo
            try {
                hapInfo = HapUtil.parse(filePath)
                this@MainViewModel.hapInfo.postValue(hapInfo)
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackBar(R.string.parse_error_fail)
            }
            // 到此为止，这个临时文件没用了，可以删掉了
            MyFileUtil.deleteExternalCacheFile(application, filePath)
        }
        isParsing.postValue(false)
    }

    fun showSnackBar(@StringRes resId: Int) {
        snackBarEvent.postValue(SnackBarEvent(application, resId))
    }

    override fun onCleared() {
        super.onCleared()
        val hapInfoValue = hapInfo.value
        hapInfoValue?.icon?.recycle()
    }
}