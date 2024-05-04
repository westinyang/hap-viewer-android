package org.ohosdev.hapviewerandroid.ui.main

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.ohosdev.hapviewerandroid.view.AdvancedRecyclerView
import org.ohosdev.hapviewerandroid.view.AdvancedRecyclerView.AdapterContextMenuInfo
import org.ohosdev.hapviewerandroid.view.list.ListItem

class PermissionsAdapter(val context: Context) :
    ListAdapter<String, PermissionsAdapter.ViewHolder>(DIFF_CALLBACK),
    AdvancedRecyclerView.ContextMenuInfoProvider<AdapterContextMenuInfo<String>> {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ListItem(context))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ViewHolder(private val listItem: ListItem) : RecyclerView.ViewHolder(listItem) {
        init {
            listItem.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            listItem.isContextClickable = true
        }

        fun bindTo(name: String) {
            listItem.title = name
        }
    }

    override fun createContextMenuInfo(view: View, position: Int): AdapterContextMenuInfo<String> =
        AdapterContextMenuInfo(KEY, position, getItem(position))

    companion object {
        const val KEY = "permissions"
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }
}