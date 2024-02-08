package org.ohosdev.hapviewerandroid.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeUtils
import org.ohosdev.hapviewerandroid.extensions.resolveBoolean
import com.google.android.material.appbar.AppBarLayout as GoogleAppBarLayout

/**
 * 修复 AndroidP 以下应用栏有阴影的问题
 * */
class AppBarLayout : GoogleAppBarLayout {

    @delegate:SuppressLint("PrivateResource")
    val isMaterial3Theme by lazy {
        context.resolveBoolean(com.google.android.material.R.attr.isMaterial3Theme, false)
    }
    val materialBackground
        get() = background.run { if (this is MaterialShapeDrawable) this else null }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @SuppressLint("RestrictedApi", "PrivateResource")
    override fun setElevation(elevation: Float) {
        // 阻止使用原始的方法调用阴影，因为MD3的应用栏不需要阴影
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || !isMaterial3Theme) {
            super.setElevation(elevation)
        } else {
            MaterialShapeUtils.setElevation(this, elevation)
        }
    }

    override fun getElevation() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || !isMaterial3Theme) {
        super.getElevation()
    } else {
        materialBackground?.elevation ?: 0f
    }

}