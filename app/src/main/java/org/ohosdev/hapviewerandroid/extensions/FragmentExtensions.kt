package org.ohosdev.hapviewerandroid.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment

fun Fragment.ensureArguments() {
    if (arguments == null) arguments = Bundle()
}