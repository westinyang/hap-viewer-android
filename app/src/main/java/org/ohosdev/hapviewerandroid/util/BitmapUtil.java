package org.ohosdev.hapviewerandroid.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import org.ohosdev.hapviewerandroid.R;

public class BitmapUtil {

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
     * @param src 原始 Bitmap
     * @return 阴影Bitmap
     */
    public static Bitmap newShadowBitmap(Context context, Bitmap src,int padding,int width,int height) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        int newWidth;
        int newHeight;
        if (srcWidth > srcHeight) {
            newWidth = width - padding * 2;
            newHeight = (int) ((float) newWidth / srcWidth * srcHeight + 0.5f);
        } else {
            newHeight = height - padding * 2;
            newWidth = (int) ((float) newHeight / srcHeight * srcWidth + 0.5f);
        }

        Matrix matrix = new Matrix();
        matrix.postScale((float) newWidth / srcWidth, (float) newHeight / srcHeight);
        Bitmap scaledSrcBitmap = Bitmap.createBitmap(src, 0, 0, srcWidth, srcHeight, matrix, false);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(0, 0, 0, 0.3f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        canvas.drawBitmap(scaledSrcBitmap, (float) (width - newWidth) / 2, (float) (height - newHeight) / 2, paint);

        blurBitmap(context,bitmap);
        return bitmap;
    }
}
