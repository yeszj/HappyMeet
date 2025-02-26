package cn.yanhu.baselib.utils;

/**
 * @author: zhengjun
 * created: 2025/2/14
 * desc:
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

public class BlurUtil {

    /**
     * 对View进行模糊处理
     *
     * @param context 上下文
     * @param view    需要模糊的View
     * @param radius  模糊半径（0 < radius <= 25）
     * @return 模糊后的Bitmap
     */
    public static Bitmap blurView(Context context, View view, float radius) {
        // 创建View的快照
        Bitmap bitmap = getViewSnapshot(view);
        if (bitmap == null) return null;

        // 使用RenderScript进行模糊处理
        Bitmap blurredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        Allocation output = Allocation.createFromBitmap(rs, blurredBitmap);
        blurScript.setRadius(radius);
        blurScript.setInput(input);
        blurScript.forEach(output);
        output.copyTo(blurredBitmap);

        // 释放资源
        bitmap.recycle();
        rs.destroy();
        return blurredBitmap;
    }

    /**
     * 获取View的快照
     *
     * @param view 需要截图的View
     * @return View的Bitmap
     */
    private static Bitmap getViewSnapshot(View view) {
        if (view == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
