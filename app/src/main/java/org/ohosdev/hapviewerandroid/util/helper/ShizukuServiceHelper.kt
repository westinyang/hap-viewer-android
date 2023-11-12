package org.ohosdev.hapviewerandroid.util.helper

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import org.ohosdev.hapviewerandroid.BuildConfig
import org.ohosdev.hapviewerandroid.IUserService
import org.ohosdev.hapviewerandroid.service.shizuku.UserService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs

class ShizukuServiceHelper {
    private var _service: IUserService? = null
    val service get() = _service!!
    val isServiceBound get() = _service != null

    private val userServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            binder.pingBinder().let {
                _service = if (it) IUserService.Stub.asInterface(binder) else null
                if (it) {
                    Log.e(TAG, "onServiceConnected: invalid binder for $componentName received")
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            _service = null
        }
    }

    private val userServiceArgs = UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    fun bindUserService(): Boolean {
        return !isServiceBound && isSupported() && runCatching {
            Shizuku.bindUserService(userServiceArgs, userServiceConnection)
        }.isSuccess
    }

    private fun unbindUserService(): Boolean {
        return isServiceBound && isSupported() && runCatching {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
        }.isSuccess
    }

    companion object {
        private const val TAG = "ShizukuHelper"

        fun isSupported(): Boolean {
            return Shizuku.getVersion() >= 10
        }
    }
}