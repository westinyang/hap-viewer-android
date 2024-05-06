package org.ohosdev.hapviewerandroid.view

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class AdvancedRecyclerView : RecyclerView {
    private var contextMenuInfo: ContextMenu.ContextMenuInfo? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? = contextMenuInfo

    override fun showContextMenuForChild(originalView: View?): Boolean {
        val adapter = adapter
        if (originalView == null || adapter == null || adapter !is ContextMenuInfoProvider<*>) return false

        val position = getChildAdapterPosition(originalView)
        if (position >= 0) {
            contextMenuInfo = adapter.createContextMenuInfo(originalView, position)
            return super.showContextMenuForChild(originalView)
        }
        contextMenuInfo = null
        return false
    }

    override fun showContextMenu(): Boolean {
        contextMenuInfo = null
        return super.showContextMenu()
    }

    override fun showContextMenu(x: Float, y: Float): Boolean {
        contextMenuInfo = null
        return super.showContextMenu(x, y)
    }

    class AdapterContextMenuInfo<T>(
        val key: String, val position: Int, val item: T
    ) : ContextMenu.ContextMenuInfo

    interface ContextMenuInfoProvider<T : ContextMenu.ContextMenuInfo> {
        fun createContextMenuInfo(view: View, position: Int): T?
    }
}