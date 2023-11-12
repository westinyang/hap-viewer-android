package org.ohosdev.hapviewerandroid.service.shizuku

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import org.ohosdev.hapviewerandroid.IUserService
import org.ohosdev.hapviewerandroid.util.ExecuteResult
import java.io.File

class UserService() : IUserService.Stub() {
    /**
     * Constructor with Context. This is only available from Shizuku API v13.
     *
     *
     * This method need to be annotated with [Keep] to prevent ProGuard from removing it.
     *[code used to create the instance of this class](https://github.com/RikkaApps/Shizuku-API/blob/672f5efd4b33c2441dbf609772627e63417587ac/server-shared/src/main/java/rikka/shizuku/server/UserService.java.L66)
     * @param context Context created with createPackageContextAsUser
     * @see [code used to create the instance of this class](https://github.com/RikkaApps/Shizuku-API/blob/672f5efd4b33c2441dbf609772627e63417587ac/server-shared/src/main/java/rikka/shizuku/server/UserService.java.L66)
     */
    @Keep
    constructor(context: Context) : this() {
        Log.i(TAG, "constructor with Context: context=$context")
    }

    override fun destroy() {
        Log.i(TAG, "destroy")
        System.exit(0)
    }

    override fun exit() {
        destroy()
    }

    override fun execute(
        cmdarray: MutableList<String>,
        envp: MutableList<String>?,
        dir: String?
    ): ExecuteResult {
        val process = Runtime.getRuntime().exec(
            cmdarray.toTypedArray(), envp?.toTypedArray(), dir?.let { File(it) }
        )
        process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return ExecuteResult(process.exitValue(), error, output)
    }

    companion object {
        private const val TAG = "UserService"
    }
}