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
 * @param height 新图片总高度
 * @param width 新图片总宽度
 * @param padding 新图片边距
 * @return 阴影Bitmap
 */
fun Bitmap.newShadowBitmap(context: Context, padding: Int = 0, width: Int, height: Int): Bitmap {
    val srcWidth = this.width
    val srcHeight = this.height
    val innerWidth: Int
    val innerHeight: Int
    // 计算新的宽高，使图片四周留出4dp边距
    if (srcWidth > srcHeight) {
        innerWidth = width - padding * 2
        innerHeight = (innerWidth.toFloat() / srcWidth * srcHeight + 0.5f).toInt()
    } else {
        innerHeight = height - padding * 2
        innerWidth = (innerHeight.toFloat() / srcHeight * srcWidth + 0.5f).toInt()
    }

    val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(newBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 添加黑色滤镜，使图片变为黑色
    val colorMatrix = ColorMatrix()
    colorMatrix.setScale(0f, 0f, 0f, 0.3f)
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    val rect = Rect(0, 0, innerWidth, innerHeight)
    rect.offset(
        ((width - innerWidth).toFloat() / 2).toInt(),
        ((height - innerHeight).toFloat() / 2).toInt()
    )
    canvas.drawBitmap(this, null, rect, paint)
    // 模糊一下图片，使图片变虚，看起来好像阴影
    newBitmap.blur(context)
    return newBitmap
}