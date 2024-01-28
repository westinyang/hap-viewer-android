package org.ohosdev.hapviewerandroid.extensions

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.resources.MaterialAttributes
import com.google.android.material.resources.MaterialAttributes.resolveBoolean
import org.ohosdev.hapviewerandroid.R

/**
 * 如果主题中已启用分割线，就应用到布局中。
 * */
@SuppressLint("RestrictedApi")
fun RecyclerView.applyDividerIfEnabled(orientation: Int = DividerItemDecoration.VERTICAL) {
    if (resolveBoolean(context, R.attr.enableDivider, false)) {
        addItemDecoration(object : MaterialDividerItemDecoration(context, orientation) {
            override fun shouldDrawDivider(position: Int, adapter: RecyclerView.Adapter<*>?) =
                adapter?.run { position != itemCount - 1 } ?: false
        })
    }
}