package org.ohosdev.hapviewerandroid.view.list

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import org.ohosdev.hapviewerandroid.R

class ListItem : FrameLayout {
    var title: String? = null
        set(value) {
            field = value
            titleTextView?.apply {
                visibility = if (value == null) GONE else VISIBLE
                text = value
            }
        }
    var valueText: String? = null
        set(value) {
            field = value
            valueTextView?.apply {
                visibility = if (value == null) GONE else VISIBLE
                text = value
            }
        }

    private val titleTextView: TextView? by lazy { findViewById(R.id.titleText) }
    private val valueTextView: TextView? by lazy { findViewById(R.id.valueText) }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.listItemStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, R.style.Widget_ListItem_Material
    )

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        context.obtainStyledAttributes(attrs, R.styleable.ListItem, defStyleAttr, defStyleRes).also {
            LayoutInflater.from(context).inflate(it.getResourceId(R.styleable.ListItem_android_layout, R.layout.item_list_material),this)
            title = it.getString(R.styleable.ListItem_android_title)
            valueText = it.getString(R.styleable.ListItem_android_value)
            isEnabled = it.getBoolean(R.styleable.ListItem_android_enabled, true)
        }.recycle()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        titleTextView?.isEnabled = enabled
        valueTextView?.isEnabled = enabled
    }

}