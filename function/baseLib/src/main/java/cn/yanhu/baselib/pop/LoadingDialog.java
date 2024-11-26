package cn.yanhu.baselib.pop;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;
import java.util.List;

import cn.yanhu.baselib.R;

/**
 * 自定义弹窗 - Java
 */
public class LoadingDialog extends Dialog {

    TextView tvLoadingTx;
    SVGAImageView ivLoading;

    public LoadingDialog(Context context) {
        this(context, R.style.loading_dialog, "请稍等...");

    }

    public LoadingDialog(Context context, String string) {
        this(context, R.style.loading_dialog, string);
    }

    public LoadingDialog(Context context, boolean close) {
        this(context, R.style.loading_dialog, "请稍等...",close);
    }

    protected LoadingDialog(Context context, int theme, String string) {
        super(context, theme);
        setCanceledOnTouchOutside(false);//点击其他区域时   true  关闭弹窗  false  不关闭弹窗
        setContentView(R.layout.dialog_loading);//加载布局
        tvLoadingTx = findViewById(R.id.tv_loading_tx);
        tvLoadingTx.setText(string);
        ivLoading = findViewById(R.id.svga_loading);
        playSvga(context,"loading_light.svga");

        getWindow().getAttributes().gravity = Gravity.CENTER;//居中显示
        getWindow().getAttributes().dimAmount = 0.5f;//背景透明度  取值范围 0 ~ 1
    }

    protected LoadingDialog(Context context, int theme, String string, boolean isOtherOnClickClose) {
        super(context, theme);
        setCanceledOnTouchOutside(false);//点击其他区域时   true  关闭弹窗  false  不关闭弹窗
        setContentView(R.layout.dialog_loading);//加载布局
        tvLoadingTx = findViewById(R.id.tv_loading_tx);
//        tvLoadingTx.setText(string);
        ivLoading = findViewById(R.id.svga_loading);
        // 使用ImageView显示动画
        playSvga(context, "loading_dark");

        getWindow().getAttributes().gravity = Gravity.CENTER;//居中显示
        getWindow().getAttributes().dimAmount = 0.5f;//背景透明度  取值范围 0 ~ 1


    }

    private void playSvga(Context context, String svgaFileName) {
        SVGAParser svgaParser = new SVGAParser(context);
        svgaParser.decodeFromAssets(svgaFileName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                SVGADrawable svgaDrawable = new SVGADrawable(svgaVideoEntity);
                // 设置drawable 资源
                ivLoading.setImageDrawable(svgaDrawable);
                // 暂停动画，停在第一个页面
                //_SVGAImageView.pauseAnimation();

                // 设置为填充模式
                svgaDrawable.setScaleType(ImageView.ScaleType.FIT_XY);

//                            activityLoginBinding.loginSvgaView.setLoops(1);
                // 开始播放动画
                ivLoading.startAnimation();
                // 设置回调
                ivLoading.setCallback(new SVGACallback() {
                    @Override
                    public void onPause() {
                        // 暂停
//                        KLog.i("动画暂停");
                    }

                    @Override
                    public void onFinished() {
                        // 完成
//                        KLog.i("动画完成");
                    }

                    @Override
                    public void onRepeat() {


                    }

                    @Override
                    public void onStep(int i, double v) {

                    }
                });
            }

            @Override
            public void onError() {
            }
        }, new SVGAParser.PlayCallback() {
            @Override
            public void onPlay(@NonNull List<? extends File> list) {

            }
        });

    }

    //关闭弹窗
    @Override
    public void dismiss() {
        ivLoading.pauseAnimation();
        ivLoading.clear();
        super.dismiss();
    }
}

