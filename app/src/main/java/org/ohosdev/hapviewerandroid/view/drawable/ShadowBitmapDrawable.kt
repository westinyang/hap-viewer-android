package org.ohosdev.hapviewerandroid.view.drawable

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.HapViewerApp
import org.ohosdev.hapviewerandroid.extensions.createBlurredBitmap
import org.ohosdev.hapviewerandroid.extensions.ratio
import org.xmlpull.v1.XmlPullParser
import kotlin.math.roundToInt

class ShadowBitmapDrawable : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setScale(0f, 0f, 0f, 1f)
        })
    }

    var shadowRadius: Float = 0f
        set(value) {
            field = value
            refreshShadowBitmap()
        }

    var shadowColor
        get() = paint.color
        set(value) {
            paint.color = value
        }

    private val shadowRadiusI get() = shadowRadius.roundToInt()

    /**
     * 原始 Bitmap，用于在大小变化后重新生成`shadowBitmap`
     */
    var originBitmap: Bitmap? = null
        set(value) {
            field = value
            refreshShadowBitmap()
        }

    private var shadowBitmap: Bitmap? = null

    /**
     * 阴影偏移量
     *
     * - 0：横向偏移量
     * - 1：纵向偏移量
     * */
    private var shadowOffset = arrayOf(0, 0)
    private val rect = Rect(0, 0, 0, 0)

    override fun draw(canvas: Canvas) {
        shadowBitmap?.let {
            canvas.drawBitmap(it, null, rect, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setScale(0f, 0f, 0f, alpha / 255f)
        })
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

    @SuppressLint("RestrictedApi")
    override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Resources.Theme?) {
        super.inflate(r, parser, attrs, theme)
        TypedArrayUtils.obtainAttributes(r, theme, attrs, R.styleable.ShadowBitmapDrawable).also {
            alpha = (it.getFloat(R.styleable.ShadowBitmapDrawable_android_alpha, 1f) * 255).toInt()
            shadowRadius = it.getDimension(R.styleable.ShadowBitmapDrawable_shadowRadius, 1f)
            shadowColor = it.getColor(R.styleable.ShadowBitmapDrawable_shadowColor, Color.BLACK)
            shadowOffset[0] = it.getDimensionPixelOffset(R.styleable.ShadowBitmapDrawable_shadowX, 0)
            shadowOffset[1] = it.getDimensionPixelOffset(R.styleable.ShadowBitmapDrawable_shadowY, 0)
        }.recycle()
    }

    private fun refreshShadowBitmap() = bounds.run {
        if (isEmpty) {
            return@run
        }
        originBitmap?.let {
            val scale = if (it.ratio > ratio) width().toFloat() / it.width else height().toFloat() / it.height
            val scaledWidth = (it.width * scale).roundToInt()
            val scaledHeight = (it.height * scale).roundToInt()
            shadowBitmap?.recycle()
            shadowBitmap = createBlurredBitmap(HapViewerApp.instance, it, width(), height(), shadowRadius)
            rect.set(
                /* left = */ bounds.centerX() - scaledWidth / 2 - shadowRadiusI,
                /* top = */ bounds.centerY() - scaledHeight / 2 - shadowRadiusI,
                /* right = */ bounds.centerX() + scaledWidth / 2 + shadowRadiusI,
                /* bottom = */ bounds.centerY() + scaledHeight / 2 + shadowRadiusI
            ).apply {
                offset(shadowOffset[0], shadowOffset[1])
            }
        }
    }

    companion object {
        private const val TAG = "ShadowBitmapDrawable"
    }
}