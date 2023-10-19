package com.wegielek.signalychinese.utils;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

public class Utils {

    public static boolean containsChinese(String input) {
        return input.matches(".*[\\u4e00-\\u9fff]+.*");
    }

    public static Paint getPaint(int color, float brushSize, boolean dashed) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(null);
        paint.setAlpha(0xff);

        paint.setColor(color);
        paint.setStrokeWidth(brushSize);

        if (dashed) {
            paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 5f));
        }

        return paint;
    }

    public static int dpToPixels(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
