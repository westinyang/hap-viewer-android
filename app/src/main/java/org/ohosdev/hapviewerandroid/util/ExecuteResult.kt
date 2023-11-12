package org.ohosdev.hapviewerandroid.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ExecuteResult(val exitCode: Int, val error: String, val output: String) : Parcelable {
    val isSuccess get() = exitCode == 0
}