package org.ohosdev.hapviewerandroid.view

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class AdvancedRecyclerView : RecyclerView {
    private var contextMenuInfo: ContextMenuInfo<*>? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return contextMenuInfo
    }

    override fun showContextMenuForChild(originalView: View?): Boolean {
        if (originalView == null || adapter == null) return false

        val longPressPosition = getChildAdapterPosition(originalView)
        if (longPressPosition >= 0) {
            contextMenuInfo =
                ContextMenuInfo(longPressPosition, getChildViewHolder(originalView))
            return super.showContextMenuForChild(originalView)
        }
        return false
    }

    class ContextMenuInfo<T : ViewHolder>(
        val position: Int,
        val viewHolder: T
    ) : ContextMenu.ContextMenuInfo
}