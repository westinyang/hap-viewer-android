package org.ohosdev.hapviewerandroid.extensions

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import org.ohosdev.hapviewerandroid.R

fun RecyclerView.applyDividerIfEnabled(orientation: Int = DividerItemDecoration.VERTICAL) {
    val dividerTypedArray = context.theme.obtainStyledAttributes(
        intArrayOf(R.attr.enableDivider)
    )

    if (dividerTypedArray.getBoolean(0, false)) {
        addItemDecoration(object :
            MaterialDividerItemDecoration(context, orientation) {
            override fun shouldDrawDivider(
                position: Int,
                adapter: RecyclerView.Adapter<*>?
            ): Boolean {
                return if (adapter != null) {
                    position != adapter.itemCount - 1
                } else false
            }
        })
    }
    dividerTypedArray.recycle()
}