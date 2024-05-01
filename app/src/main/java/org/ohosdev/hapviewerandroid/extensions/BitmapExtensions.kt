package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.annotation.FloatRange
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "BitmapExtensions"

val Bitmap.ratio get() = width.toFloat() / height
val Bitmap.widthF get() = width.toFloat()
val Bitmap.heightF get() = height.toFloat()

/**
 * 模糊Bitmap
 *
 * [https://www.jianshu.com/p/dc6120570cea](https://www.jianshu.com/p/dc6120570cea)
 *
 * (这个类虽然废弃了，但是也可以用)
 * @param context 上下文
 */
@Suppress("DEPRECATION")
fun Bitmap.blur(context: Context, @FloatRange(from = 0.0, to = 25.0) radius: Float = 8f) {
    val renderScript = RenderScript.create(context)
    val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    val input = Allocation.createFromBitmap(renderScript, this)
    val output = Allocation.createTyped(renderScript, input.type)
    scriptIntrinsicBlur.setRadius(radius)
    scriptIntrinsicBlur.setInput(input)
    scriptIntrinsicBlur.forEach(output)
    // 将数据填充到Allocation中
    output.copyTo(this)
}

/**
 * 创建已模糊的Bitmap，并且如果模糊半径大于25，则将缩小图片，并在大小上添加模糊半径
 *
 * */
fun createBlurredBitmap(
    context: Context,
    bitmap: Bitmap,
    maxWidth: Int,
    maxHeight: Int,
    @FloatRange(from = 0.0) radius: Float
): Bitmap {
    // 不超过 25 的模糊半径
    val radiusScale = if (radius > 25f) 25f / radius else 1f
    // 不超过 [maxWidth, maxHeight] 的大小
    val scale =
        if (bitmap.ratio > maxWidth.toFloat() / maxHeight) maxWidth / bitmap.widthF else maxHeight / bitmap.heightF

    Log.d(TAG, "createBlurredBitmap: $scale")
    val scaledWidth = (radiusScale * scale * bitmap.width).roundToInt()
    val scaledHeight = (radiusScale * scale * bitmap.height).roundToInt()
    val scaledRadius = min(radiusScale * radius, 25f)
    val scaledRadiusInt = scaledRadius.roundToInt()
    val doubleScaledRadiusInt = (scaledRadius * 2).roundToInt()
    val scaledBitmap = Bitmap.createBitmap(
        scaledWidth + doubleScaledRadiusInt,
        scaledHeight + doubleScaledRadiusInt,
        Bitmap.Config.ARGB_8888
    ).apply {
        Canvas(this).run {
            val rect = Rect(0, 0, scaledWidth, scaledHeight).apply {
                offset(scaledRadiusInt, scaledRadiusInt)
            }
            drawBitmap(bitmap, null, rect, null)
        }
        if (scaledRadius > 0) {
            blur(context, scaledRadius)
        }
    }
    return scaledBitmap
}