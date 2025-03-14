package cn.yanhu.imchat.custom.message.chattipview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMCustomMessageBody;

import java.util.Map;

import cn.yanhu.baselib.utils.GlideUtils;
import cn.yanhu.commonres.config.ImMessageParamsConfig;
import cn.yanhu.commonres.manager.AppCacheManager;
import cn.yanhu.commonres.router.PageIntentUtil;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.imchat.databinding.EaseTipLayoutBinding;

@SuppressLint("ViewConstructor")
public class ChatTipView extends BaseEaseChatRow {

    private EaseTipLayoutBinding binding;


    public ChatTipView(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        View inflate = inflater.inflate(R.layout.ease_tip_layout, this);
        inflate.setTag("layout/ease_tip_layout_0");
        binding = EaseTipLayoutBinding.bind(getRootView());
    }

    @Override
    protected void onFindViewById() {
    }

    @Override
    protected void onSetUpView() {
        super.onSetUpView();
        try {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) message.getBody();
            Map<String, String> params = messageBody.getParams();
            String iconUrl = params.get("leftIcon");
            if (TextUtils.isEmpty(iconUrl)) {
                binding.alertIcon.setVisibility(View.GONE);
            } else {
                binding.alertIcon.setVisibility(View.VISIBLE);
                Glide.with(context).load(iconUrl).into(binding.alertIcon);
            }

            if (params.containsKey("rightIcon")) {
                String rightIcon = params.get("rightIcon");
                if (!TextUtils.isEmpty(rightIcon)) {
                    binding.rightIcon.setVisibility(View.VISIBLE);
                    GlideUtils.loadImage(context, rightIcon, binding.rightIcon);
                } else {
                    binding.rightIcon.setVisibility(GONE);
                }
            } else {
                binding.rightIcon.setVisibility(GONE);
            }

            String content;
            if (message.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                //发送方
                content = params.get("sendShowContent");
            } else {
                content = params.get("receiveShowContent");
            }
            if (TextUtils.isEmpty(content)) {
                binding.alertView.setVisibility(View.GONE);
            } else {
                binding.alertView.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(content) && content.contains("</font>")) {
                    binding.tvAlert.setText(Html.fromHtml(content));
                } else {
                    binding.tvAlert.setText(content);
                }
            }

            if (params.containsKey(ImMessageParamsConfig.KEY_BTN_VALUE)) {
                String btnValue = params.get(ImMessageParamsConfig.KEY_BTN_VALUE);
                binding.tvOperate.setText(btnValue);
                binding.tvOperate.setVisibility(View.VISIBLE);
                binding.tvOperate.setOnClickListener(v -> {
                    String pageUrl = params.get(ImMessageParamsConfig.KEY_PAGE_URL);
                    PageIntentUtil.url2Page(context, pageUrl);
                });
            } else {
                binding.tvOperate.setVisibility(View.GONE);
            }
            binding.executePendingBindings();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}