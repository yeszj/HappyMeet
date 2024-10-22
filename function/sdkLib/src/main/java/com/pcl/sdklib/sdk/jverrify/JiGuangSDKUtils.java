package com.pcl.sdklib.sdk.jverrify;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ActivityUtils;
import com.pcl.sdklib.R;

import java.util.ArrayList;
import java.util.List;

import cn.jiguang.api.utils.JCollectionAuth;
import cn.jiguang.verifysdk.api.AuthPageEventListener;
import cn.jiguang.verifysdk.api.JVerificationInterface;
import cn.jiguang.verifysdk.api.JVerifyUIConfig;
import cn.jiguang.verifysdk.api.PrivacyBean;
import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.DisplayUtils;
import cn.yanhu.commonres.manager.WebUrlManager;
import cn.yanhu.commonres.pop.AgreementTipsPop;
import cn.zj.netrequest.application.ApplicationProxy;

public class JiGuangSDKUtils {

    public final String INIT_FAIL = "一键登录不可用，请使用验证码登录";
    public final String NETWORK_FAIL = "网络环境异常，请使用验证码登录";
    public final String LOGIN_TOKEN_GET_FAIL = "登录出错，请尝试验证码登录";

    private JiGuangSDKListener jiGuangSDKListener;

    private boolean isChecked = false;

    private static final class JiGuangSDKUtilsHolder {
        static final JiGuangSDKUtils jiGuangSDKUtils = new JiGuangSDKUtils();
    }

    public static JiGuangSDKUtils getInstance() {
        return JiGuangSDKUtilsHolder.jiGuangSDKUtils;
    }

    private JiGuangSDKUtils() {
    }

    public void setJiGuangSDKListener(JiGuangSDKListener jiGuangSDKListener) {
        this.jiGuangSDKListener = jiGuangSDKListener;
    }


    /*
     * 初始化极光SDK
     * */
    public void initJVerificationSDk() {
        //关闭SDK自启动
        JCollectionAuth.enableAutoWakeup(ApplicationProxy.instance.getApplication(), false);
        JVerificationInterface.setDebugMode(true);//true为调试模式，查看日志
        JVerificationInterface.init(ApplicationProxy.instance.getApplication(), 5000, (code, msg) -> {
            if (code != 8000) {//初始化失败
                if (jiGuangSDKListener != null) {
                    jiGuangSDKListener.fail("");
                }
                return;
            }
            boolean verifyEnable = JVerificationInterface.checkVerifyEnable(ApplicationProxy.instance.getApplication());
            if (!verifyEnable) {//判断网络环境是否支持一键登录操作
                if (jiGuangSDKListener != null) {
                    jiGuangSDKListener.fail("");
                }
                return;
            }
            getPreLogin( );
        });
    }

    /*
     * 获取预取码
     * */
    private void getPreLogin() {
        JVerificationInterface.preLogin(ApplicationProxy.instance.getApplication(), 5000, (code, content, jsonObject) -> {
            if (code == 7000) {
                if (jsonObject.has("CM2")) {
                    agreementName = "《移动认证服务条款》";
                    agreementUrl = "https://wap.cmpassport.com/resources/html/contract2.html";
                } else if (jsonObject.has("CU2")) {
                    agreementName = "《联通统一认证服务条款》";
                    agreementUrl = "https://msv6.wosms.cn/html/oauth/protocol2.html";
                } else if (jsonObject.has("CT2")) {
                    agreementName = "《天翼账号服务与隐私协议》";
                    agreementUrl = "https://e.189.cn/sdk/agreement/detail.do";
                }
                if (jiGuangSDKListener != null) {
                    jiGuangSDKListener.onPreLoginSuccess();
                }
            } else {
                if (jiGuangSDKListener != null ) {
                    jiGuangSDKListener.fail("");
                }

            }
        });
    }

    private String agreementUrl;
    private String agreementName;

    /*
     * 一键登录权限页
     * */
    public void startLogin() {

        isChecked = false;
        jVerificationViewConfig();
        startLoginAuth();
    }




    private void startLoginAuth() {
        JVerificationInterface.loginAuth(ApplicationProxy.instance.getApplication(), false, (code, content, operator, jsonObject) -> {
            if (code == 6000) {
                String token = "";
                try {
                    String[] split = content.split(":");
                    token = split[0];
                } catch (Exception e) {
                    token = content;
                }
                if (jiGuangSDKListener != null) {
                    jiGuangSDKListener.successLoginToken(token, operator);
                }
            } else if (code == 6002) {
                if (jiGuangSDKListener != null) {
                    jiGuangSDKListener.cancel();
                }
            } else {
                if (jiGuangSDKListener != null) {
                    jiGuangSDKListener.fail("");
                }
            }
        }, new AuthPageEventListener() {
            @Override
            public void onEvent(int cmd, String msg) {
                if (cmd == 1) {
                    if (jiGuangSDKListener != null) {
                        jiGuangSDKListener.cancel();
                    }
                } else if (cmd == 2) {
                } else if (cmd == 6) {
                    isChecked = true;
                } else if (cmd == 7) {
                    isChecked = false;
                }
            }
        });
    }

    /*
     * 权限页自定义配置
     * */
    private void jVerificationViewConfig() {

        View loginWaysView = LayoutInflater.from(ApplicationProxy.instance.getApplication()).inflate(R.layout.ji_guang_other_ui, null);
        int numberTop = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_430);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView ivPhoneLogin = loginWaysView.findViewById(R.id.iv_phoneLogin);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ViewGroup vgParent = loginWaysView.findViewById(R.id.vg_parent);

        RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.setMargins(0, numberTop + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_145), 0, 0);
        vgParent.setLayoutParams(mLayoutParams);
        ivPhoneLogin.setOnClickListener(v -> {
            if (checkAgreement(1)) return;
            jiGuangSDKListener.jumpPhoneLogin();
        });

        RelativeLayout.LayoutParams mLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams2.setMargins(0, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_60), 0, 0);

        View svga = LayoutInflater.from(ApplicationProxy.instance.getApplication()).inflate(com.pcl.sdklib.R.layout.include_login_svga_bg, null);
        svga.setLayoutParams(mLayoutParams2);

        View backView = LayoutInflater.from(ApplicationProxy.instance.getApplication()).inflate(R.layout.view_jiguang_webview_back, null);

        ArrayList<PrivacyBean> privacyBeans = new ArrayList<>();
        privacyBeans.add(new PrivacyBean("用户服务协议", WebUrlManager.USER_AGREEMENT, "和"));
        privacyBeans.add(new PrivacyBean("隐私政策", WebUrlManager.PRIVACY_AGREEMENT, "、"));
        privacyBeans.add(new PrivacyBean("号码认证协议", WebUrlManager.PHONE_AGREEMENT, "、"));
        JVerifyUIConfig.Builder builder = new JVerifyUIConfig.Builder();
        JVerifyUIConfig uiConfig = builder
                .setAuthBGImgPath("bg_login_bg")
                .setNavHidden(true)//导航栏隐藏
                .setLogoHidden(true)
                // .setStatusBarHidden(true)
                .setStatusBarTransparent(true)
                .setStatusBarColorWithNav(true)
                .setNumberSize(CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_24))
                .setNumberColor(0xFFFFFFFF)
                .setNumFieldOffsetY(DisplayUtils.px2dip(numberTop + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_25)))
                .setSloganTextColor(0x80FFFFFF)
                .setSloganTextSize(CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_12))
                .setSloganOffsetY(DisplayUtils.px2dip(numberTop))
                .setLogBtnOffsetY(DisplayUtils.px2dip(numberTop + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_75)))
                .setLogBtnImgPath("common_shape_click_bg")
                .setLogBtnTextSize(CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_17))
                .setLogBtnTextBold(true)
                .setLogBtnWidth(DisplayUtils.px2dip(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_328)))
                .setLogBtnHeight(DisplayUtils.px2dip(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_50)))
                .setPrivacyMarginL(20)
                .setPrivacyMarginR(20)
                .setPrivacyWithBookTitleMark(true)
                .setCheckedImgPath("svg_selected_r20")
                .setUncheckedImgPath("svg_unselected_r20")
                .setPrivacyCheckboxSize(20)
                .setPrivacyCheckboxInCenter(true)
                .setPrivacyTextSize(CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_11))
                .setPrivacyTextCenterGravity(true)
                .setAppPrivacyColor(0x80FFFFFF, 0xFFFFFFFF)
                .setPrivacyNavTitleTextColor(CommonUtils.getColor(cn.yanhu.commonres.R.color.cl_common))
                .setPrivacyStatusBarColorWithNav(true)
                .setIsPrivacyViewDarkMode(false)
                .setPrivacyNavTitleTextBold(true)
                .setPrivacyNavColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.base_bg_gray))
                .setPrivacyNavReturnBtn(backView)
                .enableHintToast(true, new JiguanToast(ApplicationProxy.instance.getApplication(), () -> checkAgreement(3)))
                .setPrivacyNameAndUrlBeanList(privacyBeans)//
                .addCustomView(loginWaysView, false, null)
                .addCustomView(svga, false, null)
                .build();
        JVerificationInterface.setCustomUIWithConfig(uiConfig);

    }


    private boolean checkAgreement(int type) {
        if (!isChecked) {
            AgreementTipsPop.showDialog(ActivityUtils.getTopActivity(), agreementUrl, agreementName, () -> {
                List<View> allChildViews = getAllChildViews();
                for (int i = 0; i < allChildViews.size(); i++) {
                    View view = allChildViews.get(i);
                    if (view instanceof CheckBox) {
                        ((CheckBox) view).setChecked(true);
                        break;
                    }
                }
                if (type == 1) {
                    jiGuangSDKListener.jumpPhoneLogin();
                } else if (type == 2) {
                   // jiGuangSDKListener.jumpWxLogin();
                }
            });
            return true;
        }
        return false;
    }

    public List<View> getAllChildViews() {
        Activity activity = ActivityUtils.getTopActivity();
        View view = activity.getWindow().getDecorView();
        return getAllChildViews(view);
    }

    private List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                allchildren.add(viewchild);
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }




    public interface JiGuangSDKListener {

        void successLoginToken(String loginToken, String operator);

        void cancel();

        void jumpPhoneLogin();

        void fail(String msg);

        void onPreLoginSuccess();
    }

    public static class JiGuangSdkCallback implements JiGuangSDKListener {
        @Override
        public void cancel() {

        }


        @Override
        public void onPreLoginSuccess() {

        }

        @Override
        public void successLoginToken(String loginToken, String operator) {

        }

        @Override
        public void jumpPhoneLogin() {

        }

        @Override
        public void fail(String msg) {

        }
    }
}
