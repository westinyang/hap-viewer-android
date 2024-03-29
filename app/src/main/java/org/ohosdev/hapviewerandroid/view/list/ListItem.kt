package org.ohosdev.hapviewerandroid.view.list

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.databinding.ItemListBinding

class ListItem : FrameLayout {
    var title: String? = null
        set(value) {
            field = value
            binding.titleText.apply {
                visibility = if (value == null) GONE else VISIBLE
                text = value
            }
        }
    var valueText: String? = null
        set(value) {
            field = value
            binding.valueText.apply {
                visibility = if (value == null) GONE else VISIBLE
                text = value
            }
        }

    private val binding: ItemListBinding =
        ItemListBinding.inflate(LayoutInflater.from(context), this, false).apply {
            addView(this.root)
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.listItemStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, R.style.Widget_ListItem_Material
    )

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        context.obtainStyledAttributes(attrs, R.styleable.ListItem, defStyleAttr, 0).also {
            title = it.getString(R.styleable.ListItem_android_title)
            valueText = it.getString(R.styleable.ListItem_android_value)
            isEnabled = it.getBoolean(R.styleable.ListItem_android_enabled, true)
        }.recycle()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.titleText.isEnabled = enabled
        binding.valueText.isEnabled = enabled
        binding.root.isEnabled = enabled
    }

}