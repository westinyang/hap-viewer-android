package org.ohosdev.hapviewerandroid.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ohosdev.hapviewerandroid.model.HapInfo

// https://developer.android.google.cn/topic/libraries/architecture/viewmodel?hl=zh-cn
class MainViewModel : ViewModel() {

    val hapInfo: MutableLiveData<HapInfo> by lazy {
        MutableLiveData(HapInfo(true))
    }

    override fun onCleared() {
        super.onCleared()
        val hapInfoValue = hapInfo.value
        hapInfoValue?.icon?.recycle()
    }
}