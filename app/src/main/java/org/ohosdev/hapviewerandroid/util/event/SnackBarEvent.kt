package org.ohosdev.hapviewerandroid.util.event

import android.content.Context
import androidx.annotation.StringRes

class SnackBarEvent(val text: String, val type: String = "default") : BaseEvent() {
    constructor(context: Context, @StringRes resId: Int) : this(context.getString(resId))
}