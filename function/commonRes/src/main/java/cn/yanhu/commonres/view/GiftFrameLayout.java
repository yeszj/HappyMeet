package cn.yanhu.commonres.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.makeramen.roundedimageview.RoundedImageView;

import cn.yanhu.baselib.utils.GlideUtils;
import cn.yanhu.commonres.R;
import cn.yanhu.commonres.bean.GiftSendModel;
import cn.yanhu.commonres.utils.GiftAnimationUtil;


/*
 * 礼物飘窗
 * */
public class GiftFrameLayout extends FrameLayout {
    private final LayoutInflater mInflater;
    RelativeLayout anim_rl;
    AppCompatImageView anim_gift, anim_header_frame;
    RoundedImageView anim_header;
    TextView anim_nickname, anim_sign;
    TextView anim_num;
    /**
     * 礼物数量的起始值
     */
    int starNum = 1;
    int repeatCount = 0;
    private boolean isShowing = false;

    public GiftFrameLayout(Context context) {
        this(context, null);
    }

    public GiftFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        initView();
    }

    private void initView() {
        View view = mInflater.inflate(R.layout.item_send_gifts, this, false);
        anim_rl = (RelativeLayout) view.findViewById(R.id.animation_person_rl);
        anim_gift = (AppCompatImageView) view.findViewById(R.id.animation_gift);
//        anim_light = (ImageView) view.findViewById(R.id.animation_light);
        anim_num = (TextView) view.findViewById(R.id.animation_num);
        anim_header = (RoundedImageView) view.findViewById(R.id.gift_userheader_iv);
        anim_header_frame = (AppCompatImageView) view.findViewById(R.id.gift_userheader_iv_frame);
        anim_nickname = (TextView) view.findViewById(R.id.gift_usernickname_tv);
        anim_sign = (TextView) view.findViewById(R.id.gift_usersign_tv);
        this.addView(view);
    }

    public void hideView() {
        anim_gift.setVisibility(INVISIBLE);
//        anim_light.setVisibility(INVISIBLE);
        anim_num.setVisibility(INVISIBLE);
    }

    public void setModel(GiftSendModel model) {
        if (0 != model.getGiftCount()) {
            this.repeatCount = model.getGiftCount();
        }
        if (!TextUtils.isEmpty(model.getNickname())) {
            anim_nickname.setText(model.getNickname());
        }
        if (!TextUtils.isEmpty(model.getPortrait())) {
            GlideUtils.loadImage(getContext(), model.getPortrait(), anim_header);
        }
        if (!TextUtils.isEmpty(model.getNobleFrame())) {
            GlideUtils.loadImage(getContext(), model.getNobleFrame(), anim_header_frame);
        }
        if (!TextUtils.isEmpty(model.getSig())) {
            anim_sign.setText(model.getSig());
        }
        if (!TextUtils.isEmpty(model.getGiftIcon())) {
            GlideUtils.loadImage(getContext(), model.getGiftIcon(), anim_gift);
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    public AnimatorSet startAnimation(final int repeatCount) {
//        hideView();
        //布局飞入
        ObjectAnimator flyFromLtoR = GiftAnimationUtil.createFlyFromLtoR(anim_rl, -getWidth(), 0, 600, new OvershootInterpolator());
        flyFromLtoR.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                GiftFrameLayout.this.setVisibility(View.VISIBLE);
                GiftFrameLayout.this.setAlpha(1f);
                isShowing = true;
                anim_num.setText("x " + 1);
            }
        });
        //礼物飞入
//        ObjectAnimator flyFromLtoR2 = GiftAnimationUtil.createFlyFromLtoR(anim_gift, -getWidth(), 0, 100, new DecelerateInterpolator());
//        flyFromLtoR2.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                anim_gift.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
////                GiftAnimationUtil.startAnimationDrawable(anim_light);
//                anim_num.setVisibility(View.VISIBLE);
//            }
//        });
        //数量增加
        ObjectAnimator scaleGiftNum = null;
        if (repeatCount > 1) {
            scaleGiftNum = GiftAnimationUtil.scaleGiftNum(anim_num, repeatCount);
            scaleGiftNum.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    anim_num.setText("x " + (++starNum));
                }
            });
        }
        //向上渐变消失
        ObjectAnimator fadeAnimator = GiftAnimationUtil.createFadeAnimator(GiftFrameLayout.this, 0, -100, 300, 400);
        fadeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                GiftFrameLayout.this.setVisibility(View.INVISIBLE);
            }
        });
        // 复原
        ObjectAnimator fadeAnimator2 = GiftAnimationUtil.createFadeAnimator(GiftFrameLayout.this, 100, 0, 20, 0);
        AnimatorSet animatorSet = null;
        if (repeatCount > 1) {
            animatorSet = GiftAnimationUtil.startAnimation(flyFromLtoR, scaleGiftNum, fadeAnimator, fadeAnimator2);
        } else {
            animatorSet = GiftAnimationUtil.startAnimation(flyFromLtoR, fadeAnimator, fadeAnimator2);

        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                starNum = 1;
                isShowing = false;
            }
        });
        return animatorSet;
    }
}
