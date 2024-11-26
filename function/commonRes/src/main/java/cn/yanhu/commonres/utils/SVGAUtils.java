package cn.yanhu.commonres.utils;

import androidx.annotation.NonNull;

import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.net.URL;

/**
 * @author: witness
 * created: 2022/8/11
 * desc:
 */
public class SVGAUtils {
    public static void loadAssetsSVGAAnim(SVGAImageView svgaImageView,String svgaName) {
        try {
            SVGAParser.Companion.shareParser().decodeFromAssets(svgaName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    svgaImageView.setVideoItem(svgaVideoEntity);
                    svgaImageView.startAnimation();
                }

                @Override
                public void onError() {

                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSVGAAnim(SVGAImageView svgaImageView,String url) {
        try {
            SVGAParser.Companion.shareParser().decodeFromURL(new URL(url), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    svgaImageView.setVideoItem(svgaVideoEntity);
                    svgaImageView.startAnimation();
                }

                @Override
                public void onError() {

                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSVGAAnim(SVGAImageView svgaImageView,String url,SVGAParser.ParseCompletion parseCompletion) {
        try {
            SVGAParser.Companion.shareParser().decodeFromURL(new URL(url), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    parseCompletion.onComplete(svgaVideoEntity);
                    svgaImageView.setVideoItem(svgaVideoEntity);
                    svgaImageView.startAnimation();
                }
                @Override
                public void onError() {
                    parseCompletion.onError();

                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadCustomSVGAAnim(String url,SVGAParser.ParseCompletion parseCompletion) {
        try {
            SVGAParser.Companion.shareParser().decodeFromURL(new URL(url), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    parseCompletion.onComplete(svgaVideoEntity);
                }
                @Override
                public void onError() {
                    parseCompletion.onError();

                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
