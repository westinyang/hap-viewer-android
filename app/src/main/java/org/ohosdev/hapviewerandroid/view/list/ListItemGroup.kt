package org.ohosdev.hapviewerandroid.view.list

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import android.widget.LinearLayout
import org.ohosdev.hapviewerandroid.R

class ListItemGroup : LinearLayout {
    private var contextMenuInfo: ContextMenuInfo? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(
        context, attrs, R.attr.listItemGroupStyle
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, R.style.Widget_ListItemGroup_Material
    )

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return contextMenuInfo
    }

    override fun showContextMenuForChild(originalView: View?): Boolean {
        if (originalView is ListItem) {
            contextMenuInfo = ContextMenuInfo(originalView.title, originalView.valueText)
            return super.showContextMenuForChild(originalView)
        }
        return false
    }

    class ContextMenuInfo(val title: String?, val valueText: String?) :
        ContextMenu.ContextMenuInfo
}