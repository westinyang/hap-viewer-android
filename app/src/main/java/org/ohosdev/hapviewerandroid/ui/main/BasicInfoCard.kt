package org.ohosdev.hapviewerandroid.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import com.google.android.material.card.MaterialCardView

class BasicInfoCard : MaterialCardView {
    private val contextMenuInfo: ContextMenuInfo? by lazy { ContextMenuInfo() }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return contextMenuInfo
    }

    class ContextMenuInfo : ContextMenu.ContextMenuInfo
}