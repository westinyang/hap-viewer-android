package org.ohosdev.hapviewerandroid.view.list

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import android.widget.LinearLayout
import org.ohosdev.hapviewerandroid.R

class ListItemGroup : LinearLayout {
    private var contextMenuInfo: ContextMenuInfo? = null
    var key: String = ""

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context, attrs, R.attr.listItemGroupStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, R.style.Widget_ListItemGroup_Material
    )

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        context.obtainStyledAttributes(attrs, R.styleable.ListItemGroup, defStyleAttr, defStyleRes).also {
            key = it.getString(R.styleable.ListItemGroup_android_key) ?: ""
        }.recycle()
    }


    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? = contextMenuInfo

    override fun showContextMenuForChild(originalView: View?): Boolean {
        if (originalView is ListItem) {
            contextMenuInfo = ContextMenuInfo(key, originalView.title, originalView.valueText)
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

    class ContextMenuInfo(val key: String, val title: String?, val valueText: String?) :
        ContextMenu.ContextMenuInfo
}