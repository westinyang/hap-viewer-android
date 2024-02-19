package org.ohosdev.hapviewerandroid.harmonystyle.drawable

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.core.content.res.TypedArrayUtils.obtainAttributes
import org.ohosdev.hapviewerandroid.harmonystyle.R
import org.xmlpull.v1.XmlPullParser

@Keep
class ShadowDrawable : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
    }

    private var radius = 0f
    private var shadowRadius = 10f
    private val doubleShadowRadiusInt get() = (shadowRadius * 2).toInt()
    private var shadowColor = Color.BLACK

    /**
     * 阴影偏移量
     *
     * - 0：横向偏移量
     * - 1：纵向偏移量
     * */
    private var shadowOffset = arrayOf(0f, 0f)

    // 低版本安卓绘制阴影不能开启硬件加速
    private val noHardwareBitmap by lazy {
        Bitmap.createBitmap(
            bounds.width() + doubleShadowRadiusInt,
            bounds.height() + doubleShadowRadiusInt,
            Bitmap.Config.ARGB_8888
        ).also {
            noHardwareCanvas.setBitmap(it)
        }
    }
    private var noHardwareCanvas = Canvas()

    override fun draw(canvas: Canvas) {
        // 清空画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        noHardwareCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // 设置阴影
        paint.setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        // 绘图并从没有硬件加速的`noHardwareCanvas`复制到`canvas`
        noHardwareCanvas.drawRoundRect(
            RectF(
                shadowRadius,
                shadowRadius,
                bounds.width() + shadowRadius,
                bounds.height() + shadowRadius
            ), radius, radius, paint
        )
        canvas.drawBitmap(
            noHardwareBitmap,
            -shadowRadius + shadowOffset[0],
            -shadowRadius + shadowOffset[1],
            null
        )
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        noHardwareBitmap.apply {
            width = bounds.width() + doubleShadowRadiusInt
            height = bounds.height() + doubleShadowRadiusInt
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.setColorFilter(colorFilter)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    fun setRadius(radius: Float) {
        this.radius = radius
    }

    fun setShadowColor(color: Int) {
        shadowColor = color
    }

    @SuppressLint("RestrictedApi")
    override fun inflate(
        r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Resources.Theme?
    ) {
        super.inflate(r, parser, attrs, theme)
        obtainAttributes(r, theme, attrs, R.styleable.ShadowDrawable).also {
            radius = it.getDimension(R.styleable.ShadowDrawable_android_radius, radius)
            shadowRadius = it.getDimension(R.styleable.ShadowDrawable_shadowRadius, radius)
            shadowColor = it.getColor(R.styleable.ShadowDrawable_shadowColor, shadowColor)
            shadowOffset[0] = it.getDimension(R.styleable.ShadowDrawable_shadowX, 0f)
            shadowOffset[1] = it.getDimension(R.styleable.ShadowDrawable_shadowY, 0f)
        }.recycle()
    }
}