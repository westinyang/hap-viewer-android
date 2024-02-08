package org.ohosdev.hapviewerandroid.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.ohosdev.hapviewerandroid.util.ShizukuUtil.ShizukuStatus.ERROR
import org.ohosdev.hapviewerandroid.util.ShizukuUtil.ShizukuStatus.GRANTED
import org.ohosdev.hapviewerandroid.util.ShizukuUtil.ShizukuStatus.NOT_GRANTED
import org.ohosdev.hapviewerandroid.util.ShizukuUtil.ShizukuStatus.NOT_SUPPORT
import org.ohosdev.hapviewerandroid.util.ShizukuUtil.ShizukuStatus.SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE
import rikka.shizuku.Shizuku


class ShizukuUtil {
    companion object {
        const val PERMISSION = "moe.shizuku.manager.permission.API_V23"
        const val URL_GUIDE = "https://shizuku.rikka.app/zh-hans/guide/setup/"

        fun checkPermission() = runCatching {
            return@runCatching if (Shizuku.isPreV11()) {
                NOT_SUPPORT
            } else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                GRANTED
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE
            } else {
                NOT_GRANTED
            }
        }.getOrElse { ERROR }
    }

    enum class ShizukuStatus {
        NOT_SUPPORT,
        SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE,
        GRANTED,
        NOT_GRANTED,
        ERROR
    }

    class ShizukuLifecycleObserver : DefaultLifecycleObserver {
        private val onRequestPermissionResultListener =
            Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
                requestPermissionResultListener?.onRequestPermissionResult(requestCode, grantResult)
            }
        private val onBinderReceivedListener =
            Shizuku.OnBinderReceivedListener { binderReceivedListener?.onBinderReceived() }
        private val onBinderDeadListener = Shizuku.OnBinderDeadListener { binderDeadListener?.onBinderDead() }

        private var requestPermissionResultListener: Shizuku.OnRequestPermissionResultListener? = null
        private var binderReceivedListener: Shizuku.OnBinderReceivedListener? = null
        private var binderDeadListener: Shizuku.OnBinderDeadListener? = null

        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)
            Shizuku.addBinderReceivedListener(onBinderReceivedListener)
            Shizuku.addBinderDeadListener(onBinderDeadListener)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener)
            Shizuku.removeBinderReceivedListener(onBinderReceivedListener)
            Shizuku.removeBinderDeadListener(onBinderDeadListener)
        }

        fun setRequestPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
            requestPermissionResultListener = listener
        }

        fun setBinderReceivedListener(listener: Shizuku.OnBinderReceivedListener) {
            binderReceivedListener = listener
        }

        fun setBinderDeadListener(listener: Shizuku.OnBinderDeadListener) {
            binderDeadListener = listener
        }
    }
}