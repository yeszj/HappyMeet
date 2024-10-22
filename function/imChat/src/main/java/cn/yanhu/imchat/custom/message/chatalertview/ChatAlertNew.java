package cn.yanhu.imchat.custom.message.chatalertview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.easeui.EaseIM;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.Map;

import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.baselib.utils.GlideUtils;
import cn.yanhu.commonres.config.ImMessageParamsConfig;
import cn.yanhu.commonres.manager.AppCacheManager;
import cn.yanhu.commonres.manager.ImageThumbUtils;
import cn.yanhu.commonres.router.PageIntentUtil;
import cn.yanhu.commonres.utils.MediaPlayUtils;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.bean.CmdMsgInfo;
import cn.yanhu.imchat.databinding.EaseAlertLayoutBinding;

@SuppressLint("ViewConstructor")
public class ChatAlertNew extends BaseEaseChatRow {

    private EaseAlertLayoutBinding binding;
    private ImageView icon;
    private ImageView ivRightIcon;
    private TextView hintView;

    private LinearLayout alert_ll;
    private LinearLayout call_ll;

    private TextView tvCallBtn;

    private TextView tvOperate;


    public ChatAlertNew(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        View inflate = inflater.inflate(R.layout.ease_alert_layout, this);
        inflate.setTag("layout/ease_alert_layout_0");
        binding = EaseAlertLayoutBinding.bind(getRootView());
    }

    @Override
    protected void onFindViewById() {
        alert_ll = findViewById(R.id.alert_ll);
        icon = findViewById(R.id.alert_icon);
        hintView = (TextView) findViewById(R.id.tv_alert);
        call_ll = (LinearLayout) findViewById(R.id.call_ll);
        ivRightIcon = findViewById(R.id.rightIcon);
        tvOperate = findViewById(R.id.tv_operate);
        tvCallBtn = findViewById(R.id.tv_callBtn);
    }

    @Override
    protected void onSetUpView() {
        super.onSetUpView();
        try {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) message.getBody();
            Map<String, String> params = messageBody.getParams();
            String iconUrl = params.get("icon");
            if (TextUtils.isEmpty(iconUrl)){
                icon.setVisibility(View.GONE);
            }else {
                icon.setVisibility(View.VISIBLE);
                Glide.with(context).load(iconUrl).into(icon);
            }

            if (params.containsKey("rightIcon")) {
                String rightIcon = params.get("rightIcon");
                if (!TextUtils.isEmpty(rightIcon)) {
                    ivRightIcon.setVisibility(View.VISIBLE);
                    GlideUtils.loadImage(context,rightIcon,ivRightIcon);
                } else {
                    ivRightIcon.setVisibility(GONE);
                }
            } else {
                ivRightIcon.setVisibility(GONE);
            }


            String content = params.get("content");
            if (TextUtils.isEmpty(content)) {
                binding.alertView.setVisibility(View.GONE);
            } else {
                binding.alertView.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(content) && content.contains("</font>")) {
                    if (params.containsKey(ImMessageParamsConfig.KEY_PAGE_URL)){
                        String pageUrl = params.get(ImMessageParamsConfig.KEY_PAGE_URL);
                        if (!TextUtils.isEmpty(pageUrl)){
                            hintView.setOnClickListener(v -> PageIntentUtil.url2Page(context,pageUrl));
                        }
                    }
                    hintView.setText(Html.fromHtml(content));
                } else {
                    hintView.setText(content);
                }
            }

            String msgType = params.get("msgType");
            if (!TextUtils.isEmpty(msgType)) {
                if (CmdMsgInfo.MSG_TYPE_CALL.equals(msgType)) {
//                    if (AppCacheManager.getHasVideoCard()) {
//                        tvCallBtn.setText("免费视频");
//                    } else {
//                        tvCallBtn.setText("视频看看");
//                    }
                    call_ll.setVisibility(View.VISIBLE);
                    call_ll.setOnClickListener(v -> {
                        LiveEventBus.get("call_phone").post(1);
                    });
                } else {
                    call_ll.setVisibility(View.GONE);
                }
            } else {
                if (!TextUtils.isEmpty(content) && content.contains("音视频")) {
                    call_ll.setVisibility(View.VISIBLE);
//                    if (AppCacheManager.getHasVideoCard()) {
//                        tvCallBtn.setText("免费视频");
//                    } else {
//                        tvCallBtn.setText("视频看看");
//                    }
                    call_ll.setOnClickListener(v -> {
                        LiveEventBus.get("call_phone").post( 1);
                    });
                } else {
                    call_ll.setVisibility(View.GONE);
                }
            }

            if (params.containsKey(ImMessageParamsConfig.KEY_BTN_VALUE)) {
                String btnValue = params.get(ImMessageParamsConfig.KEY_BTN_VALUE);
                tvOperate.setText(btnValue);
                tvOperate.setVisibility(View.VISIBLE);
                tvOperate.setOnClickListener(v -> {
                    String pageUrl = params.get(ImMessageParamsConfig.KEY_PAGE_URL);
                    PageIntentUtil.url2Page(context, pageUrl);
                });
            } else {
                tvOperate.setVisibility(View.GONE);
            }

            String introduction = params.get("introduction");
            if (!TextUtils.isEmpty(introduction) && AppCacheManager.isMan()) {
                binding.setIntroduction(introduction);
                if (introduction.contains(".mp4")) {
                    GlideUtils.loadImage(context, ImageThumbUtils.getThumbUrl(introduction),binding.introducePortrait);

                } else {
                    GlideUtils.loadImage(context, ImageThumbUtils.getThumbUrl(EaseIM.getInstance().getUserProvider().getUser(message.getUserName()).getAvatar()),binding.introducePortrait);
                }

                binding.introduceLl.setOnClickListener(v -> {
                    if (introduction.contains(".mp4")) {
                        LiveEventBus.get("slide_introduce").post( 0);

                    } else {
                        if (MediaPlayUtils.isPlaying()){
                            binding.voiceTxt.setText("播放语音");
                            binding.ivStatus.setImageResource(R.mipmap.ic_play_stop);
                            binding.voiceLottie.pauseAnimation();
                            binding.voiceLottie.setFrame(1);
                            MediaPlayUtils.pauseMedia();
                        }else {
                            binding.voiceTxt.setText("暂停播放");
                            binding.ivStatus.setImageResource(cn.yanhu.commonres.R.mipmap.ic_play_start);
                            binding.voiceLottie.playAnimation();
                            if (MediaPlayUtils.isPause()){
                                MediaPlayUtils.resumeMedia();
                            }else {
                                MediaPlayUtils.playSound(introduction, mp -> {
                                    binding.voiceTxt.setText("播放语音");
                                    binding.ivStatus.setImageResource(R.mipmap.ic_play_stop);
                                }, null);
                            }
                        }
                    }
                });
            } else {
                binding.introduceLl.setVisibility(View.GONE);
                binding.setIntroduction("");
            }
            binding.executePendingBindings();

        } catch (Exception e) {
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MediaPlayUtils.release();
    }
}