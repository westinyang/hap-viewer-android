package org.ohosdev.hapviewerandroid.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView as AndroiXNestedScrollView

class NestedScrollView : AndroiXNestedScrollView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * 是否跟随焦点滚动
     * */
    var isScrollWithFocus: Boolean = true

    override fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect?): Int {
        // 解决自动跟随焦点滚动问题
        // https://blog.csdn.net/ZYJWR/article/details/108386309
        return if (isScrollWithFocus) super.computeScrollDeltaToGetChildRectOnScreen(rect) else 0
    }
}