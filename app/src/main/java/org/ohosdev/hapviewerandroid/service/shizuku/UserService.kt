package org.ohosdev.hapviewerandroid.service.shizuku

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.ohosdev.hapviewerandroid.IUserService
import org.ohosdev.hapviewerandroid.util.ExecuteResult
import java.io.File
import kotlin.system.exitProcess

class UserService() : IUserService.Stub() {
    @Keep
    constructor(context: Context) : this() {
        Log.i(TAG, "constructor with Context: context=$context")
    }

    override fun destroy() {
        Log.i(TAG, "destroy")
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }

    override fun execute(
        cmdarray: MutableList<String>,
        envp: MutableList<String>?,
        dir: String?
    ): ExecuteResult = runBlocking(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(
            cmdarray.toTypedArray(), envp?.toTypedArray(), dir?.let { File(it) }
        )
        val exitCode = process.waitFor()
        val error = process.errorStream.readBytes().decodeToString()
        val output = process.inputStream.readBytes().decodeToString()
        return@runBlocking ExecuteResult(exitCode, error, output)
    }


    companion object {
        private const val TAG = "UserService"
    }
}