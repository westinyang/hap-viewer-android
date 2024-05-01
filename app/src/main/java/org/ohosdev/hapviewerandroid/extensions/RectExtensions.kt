package org.ohosdev.hapviewerandroid.extensions

import android.graphics.Rect

val Rect.ratio get() = width().toFloat() / height()