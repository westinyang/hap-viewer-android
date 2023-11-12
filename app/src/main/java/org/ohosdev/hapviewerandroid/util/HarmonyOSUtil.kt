package org.ohosdev.hapviewerandroid.util


class HarmonyOSUtil {
    companion object {
        val isHarmonyOS by lazy {
            runCatching { Class.forName("ohos.app.Application") }.isSuccess
        }
    }
}
