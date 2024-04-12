package cn.yanhu.commonres.view.floatScreenView;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import cn.yanhu.baselib.R;


/**
 * @author: witness
 * created: 2022/8/4
 * desc:
 */
public class RewardAnimUtils {

    /**
     * 获取礼物入场动画
     *
     * @return
     */
    public static Animation getInAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.gift_in);
    }

    /**
     * 获取礼物出场动画
     *
     * @return
     */
    public static AnimationSet getOutAnimation(Context context) {
        return (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.gift_out);
    }


    public static Animation getUserEnterInAnimation(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.uer_enter_in);
    }

    /**
     * 获取礼物出场动画
     *
     * @return
     */
    public static AnimationSet getUserEnterOutAnimation(Context context) {
        return (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.uer_enter_out);
    }

}
