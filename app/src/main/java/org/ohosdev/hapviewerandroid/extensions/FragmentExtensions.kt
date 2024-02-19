package org.ohosdev.hapviewerandroid.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * 确保存在参数Bundle。如果不存在，则创建一个空Bundle。
 * */
fun Fragment.ensureArguments(): Bundle {
    arguments?.let { return it }
    return Bundle().apply { arguments = this }
}