package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * 模糊Bitmap
 *
 * [https://www.jianshu.com/p/dc6120570cea](https://www.jianshu.com/p/dc6120570cea)
 *
 * (这个类虽然废弃了，但是也可以用)
 * @param context 上下文
 */
fun Bitmap.blur(context: Context, radius: Float = 8f) {
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
 * 创建阴影 Bitmap
 *
 * @param context 上下文对象
 * @param height 视图高度，不包括阴影
 * @param width 视图宽度，不包括阴影
 * @param shadowRadius 阴影半径
 * @return 阴影Bitmap
 */
fun Bitmap.newShadowBitmap(
    context: Context,
    shadowRadius: Float = 0f,
    width: Int,
    height: Int
): Bitmap {
    val srcWidth = this.width
    val srcHeight = this.height
    val scaledWidth: Int
    val scaledHeight: Int

    if (srcWidth.toFloat() / srcHeight > width.toFloat() / height) {
        scaledWidth = width
        scaledHeight = (width.toFloat() / srcWidth * srcHeight + 0.5f).toInt()
    } else {
        scaledHeight = height
        scaledWidth = (height.toFloat() / srcHeight * srcWidth + 0.5f).toInt()
    }

    /* val innerWidth: Int
    val innerHeight: Int
    // 计算新的宽高，使图片四周留出2*shadowRadius边距
    if (srcWidth > srcHeight) {
        innerWidth = (width - shadowRadius * 2).toInt()
        innerHeight = (innerWidth.toFloat() / srcWidth * srcHeight).roundToInt()
    } else {
        innerHeight = (height - shadowRadius * 2).toInt()
        innerWidth = (innerHeight.toFloat() / srcHeight * srcWidth).roundToInt()
    } */

    val newBitmap = Bitmap.createBitmap(
        (width + shadowRadius * 2 + 0.5f).toInt(),
        (height + shadowRadius * 2 + 0.5f).toInt(),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(newBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 添加黑色滤镜，使图片变为黑色
    val colorMatrix = ColorMatrix()
    colorMatrix.setScale(0f, 0f, 0f, 0.3f)
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    val rect = Rect(0, 0, scaledWidth, scaledHeight)
    rect.offset(
        ((width - scaledWidth) / 2 + shadowRadius).toInt(),
        ((height - scaledHeight) / 2 + shadowRadius).toInt()
    )
    canvas.drawBitmap(this, null, rect, paint)
    // 模糊一下图片，使图片变虚，看起来好像阴影
    newBitmap.blur(context, shadowRadius)
    return newBitmap
}