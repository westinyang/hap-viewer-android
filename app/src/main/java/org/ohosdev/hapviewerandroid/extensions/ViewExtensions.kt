package org.ohosdev.hapviewerandroid.extensions

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.View

inline fun <reified M : ContextMenuInfo> View.setOnCreateContextMenuListenerWithInfo(
    crossinline block: ContextMenu.(M) -> Unit
) {
    setOnCreateContextMenuListener { menu: ContextMenu, _: View, menuInfo: ContextMenuInfo? ->
        if (menuInfo is M) {
            menu.block(menuInfo)
        }
    }
}