package org.ohosdev.hapviewerandroid.util

import android.os.Build

object SystemUtil {
    val isOhosSupported by lazy {
        runCatching { Class.forName("ohos.app.Application") }.isSuccess
    }
    val isEmui by lazy {
        runCatching { Class.forName("androidhwext.R") }.isSuccess
    }
    val isMagic by lazy {
        runCatching { Class.forName("androidhnext.R") }.isSuccess
    }
    val isDarkNavigationBarSupported by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || isEmui || isMagic
    }
}