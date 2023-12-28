package org.ohosdev.hapviewerandroid.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.BaseActivity
import org.ohosdev.hapviewerandroid.databinding.ItemInfoBinding
import org.ohosdev.hapviewerandroid.extensions.copyText
import org.ohosdev.hapviewerandroid.model.HapInfo

class InfoAdapter(val context: BaseActivity) : RecyclerView.Adapter<InfoAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = context.layoutInflater
    private val unknownString: String = context.getString(android.R.string.unknownName)
    private var info = HapInfo()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemInfoBinding.inflate(layoutInflater, parent, false))
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

    inner class ViewHolder(private val binding: ItemInfoBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        var name = ""
        var content = ""

        init {
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        private fun refresh() {
            binding.textView.text = "${name}: $content"
        }

        private fun setName(resId: Int) {
            name = context.getString(resId)
        }

        private fun copyText() {
            if (content.isEmpty()) return
            context.copyText(content)
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