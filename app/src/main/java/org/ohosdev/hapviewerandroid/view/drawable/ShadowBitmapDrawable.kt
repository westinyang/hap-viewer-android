package org.ohosdev.hapviewerandroid.view.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import org.ohosdev.hapviewerandroid.app.HapViewerApp
import org.ohosdev.hapviewerandroid.extensions.newShadowBitmap

class ShadowBitmapDrawable : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
    }

    private var shadowRadius = 0f

    /**
     * 原始 Bitmap，用于在大小变化后重新生成`shadowBitmap`
     */
    private var originBitmap: Bitmap? = null

    private var shadowBitmap: Bitmap? = null

    override fun draw(canvas: Canvas) {
        if (shadowBitmap == null) {
            return
        }
        canvas.drawBitmap(shadowBitmap!!, -shadowRadius, -shadowRadius, null)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        refreshShadowBitmap()
    }

    /**
     * 直接设置阴影图片
     * @param shadowRadius 用于偏移`bitmap`，使阴影位于View正下方
     * */
    /* fun setShadowBitmap(bitmap: Bitmap, shadowRadius: Float) {
        shadowBitmap = bitmap
        this.shadowRadius = shadowRadius
        originBitmap = null
    } */

    fun setShadowBitmap(bitmap: Bitmap, shadowRadius: Float) {
        originBitmap = bitmap
        this.shadowRadius = shadowRadius
        shadowBitmap = null
        refreshShadowBitmap()
    }

    private fun refreshShadowBitmap() = bounds.run {
        if (isEmpty) {
            return
        }
        if (originBitmap != null) {
            shadowBitmap?.recycle()
            shadowBitmap = originBitmap!!.newShadowBitmap(
                HapViewerApp.instance,
                shadowRadius,
                width(),
                height()
            )
        }
    }
}