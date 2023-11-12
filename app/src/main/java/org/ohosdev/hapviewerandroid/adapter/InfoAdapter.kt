package org.ohosdev.hapviewerandroid.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.BaseActivity
import org.ohosdev.hapviewerandroid.model.HapInfo

class InfoAdapter(val context: BaseActivity) : RecyclerView.Adapter<InfoAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val unknownString: String = context.getString(android.R.string.unknownName)
    private var info = HapInfo()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_info, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> holder.bind(R.string.info_appName, info.appName)
            1 -> holder.bind(R.string.info_appPackageName, info.packageName)
            2 -> holder.bind(R.string.info_versionName, info.versionName)
            3 -> holder.bind(R.string.info_versionCode, info.versionCode)
            4 -> holder.bind(
                R.string.info_compileTarget, "API ${info.targetAPIVersion} (${info.apiReleaseType})"
            )

            5 -> holder.bind(R.string.info_tech, info.getTechDesc(context))
        }
    }

    override fun getItemCount(): Int {
        return MAX_ITEMS
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setInfo(info: HapInfo) {
        this.info = info
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private var _name = ""
        private var _content = ""
        var name
            get() = _name
            set(value) {
                _name = value
            }

        var content
            get() = _content
            set(value) {
                _content = value
            }

        init {
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        private fun refresh() {
            textView.text = "${name}: $content"
        }

        private fun setName(resId: Int) {
            name = context.getString(resId)
        }

        private fun copyText() {
            if (content.isEmpty()) return
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(null, content))

            val toastText = context.getString(R.string.copied_withName, name)
            context.showSnackBar(toastText)
        }

        override fun onClick(v: View) {
            copyText()
        }

        fun bind(nameId: Int, content: String?) {
            setName(nameId)
            this.content = if (info.init || content == null) unknownString else content
            refresh()
        }
    }

    companion object {
        private const val MAX_ITEMS = 6
    }
}