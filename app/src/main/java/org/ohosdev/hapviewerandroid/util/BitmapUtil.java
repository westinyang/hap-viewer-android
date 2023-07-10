package org.ohosdev.hapviewerandroid.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    /**
     * <a href="https://www.jianshu.com/p/dc6120570cea">https://www.jianshu.com/p/dc6120570cea</a>
     * (这个类虽然废弃了，但是也可以用)
     *
     * @param context
     * @param bitmap
     */
    public static void blurBitmap(Context context, Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(context);
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
        Allocation output = Allocation.createTyped(renderScript, input.getType());
        scriptIntrinsicBlur.setRadius(8f);
        scriptIntrinsicBlur.setInput(input);
        scriptIntrinsicBlur.forEach(output);

        // 将数据填充到Allocation中
        output.copyTo(bitmap);
    }

    public static Bitmap newMaskBitmap(Context context, Bitmap src) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(0, 0, 0, 0.3f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmap;
    }

    /**
     * 创建阴影 Bitmap
     *
     * @param context 上下文对象
     * @param src 原始 Bitmap
     * @param height 新图片高度
     * @param width 新图片宽度
     * @param padding 新图片边距
     * @return 阴影Bitmap
     */
    public static Bitmap newShadowBitmap(Context context, Bitmap src, int padding, int width, int height) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        int newWidth;
        int newHeight;
        // 计算新的宽高，使图片四周留出4dp边距
        if (srcWidth > srcHeight) {
            newWidth = width - padding * 2;
            newHeight = (int) ((float) newWidth / srcWidth * srcHeight + 0.5f);
        } else {
            newHeight = height - padding * 2;
            newWidth = (int) ((float) newHeight / srcHeight * srcWidth + 0.5f);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 添加黑色滤镜，使图片变为黑色
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(0, 0, 0, 0.3f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        Rect rect = new Rect(0, 0, newWidth, newHeight);
        rect.offset((int) ((float) (width - newWidth) / 2), (int) ((float) (height - newHeight) / 2));
        canvas.drawBitmap(src, null, rect, paint);
        //模糊一下图片，使图片变虚，看起来好像阴影
        blurBitmap(context, bitmap);
        return bitmap;
    }
}
