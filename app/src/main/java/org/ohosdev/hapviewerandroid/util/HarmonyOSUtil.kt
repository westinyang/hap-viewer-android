package org.ohosdev.hapviewerandroid.util

import org.ohosdev.hapviewerandroid.extensions.isFileInSystemPath

class HarmonyOSUtil {
    companion object {
        val isBmSystemPath by lazy { "bm".isFileInSystemPath() }
        val isHarmonyOS by lazy {
            runCatching { Class.forName("ohos.app.Application") }.isSuccess
        }
    }
}
