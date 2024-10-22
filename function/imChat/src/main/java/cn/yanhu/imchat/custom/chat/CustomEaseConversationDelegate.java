package cn.yanhu.imchat.custom.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.GsonUtils;
import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EasePreferenceManager;
import com.hyphenate.easeui.modules.conversation.delegate.EaseDefaultConversationDelegate;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationSetStyle;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.EaseDateUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

import java.util.Date;
import java.util.Objects;

import cn.yanhu.commonres.bean.UserDetailInfo;
import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.commonres.manager.ImageThumbUtils;
import cn.yanhu.imchat.bean.CacheConversationInfo;
import cn.yanhu.imchat.bean.ConversationFinalMessageInfo;
import cn.yanhu.imchat.databinding.EaseItemRowChatHistoryBinding;
import cn.yanhu.imchat.manager.ImMsgManager;

public class CustomEaseConversationDelegate extends EaseDefaultConversationDelegate {

    private EaseItemRowChatHistoryBinding binding;


    public CustomEaseConversationDelegate(EaseConversationSetStyle setModel) {
        super(setModel);
    }

    @Override
    public boolean isForViewType(EaseConversationInfo item, int position) {
        return item != null && item.getInfo() instanceof EMConversation;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, String tag) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ease_item_row_chat_history, parent, false);
        return new ViewHolder(view, setModel);
    }

    @Override
    protected void onBindConViewHolder(ViewHolder holder, int position, EaseConversationInfo bean) {
        binding = DataBindingUtil.bind(holder.itemView);
        CustomConversationInfo conversationInfo = GsonUtils.fromJson(GsonUtils.toJson(bean), CustomConversationInfo.class);
        Context context = holder.itemView.getContext();
        holder.listIteaseLayout.setBackground(bean.isTop() ? ContextCompat.getDrawable(context, R.drawable.ease_conversation_top_bg) : ContextCompat.getDrawable(context, cn.yanhu.commonres.R.drawable.shape_transparent));
        String conversationId;
        if (conversationInfo.isLoadCache()) {
            CacheConversationInfo cacheConversationInfo = conversationInfo.getCacheConversationInfo();
            conversationId = cacheConversationInfo.getConversationId();
            ConversationFinalMessageInfo cacheMessage = cacheConversationInfo.getCacheMessage();
            ConversationFinalMessageInfo finalMessage = conversationInfo.getFinalMessage();
            bindFinalMessage(holder, context, Objects.requireNonNullElse(finalMessage, cacheMessage));
            showUnreadNum(holder, cacheConversationInfo.getUnReadMsgCount());
        } else {
            EMConversation item = (EMConversation) bean.getInfo();
            conversationId = item.conversationId();
            if (!setModel.isHideUnreadDot()) {
                if (conversationInfo.isClearUnReadCount()) {
                    showUnreadNum(holder, 0);
                } else {
                    showUnreadNum(holder, item.getUnreadMsgCount());
                }
            }

            EMMessage lastMessage = item.getLastMessage();

            if (lastMessage != null) {
                setContent(holder, context, lastMessage, conversationInfo.isGroup());
                if (!conversationInfo.isGroup()) {
                    ConversationFinalMessageInfo finalMessage = conversationInfo.getFinalMessage();
                    bindFinalMessage(holder, context, finalMessage);
                }

                if (lastMessage.direct() == EMMessage.Direct.SEND && lastMessage.status() == EMMessage.Status.FAIL) {
                    binding.tvFail.setVisibility(View.VISIBLE);
                } else {
                    binding.tvFail.setVisibility(View.GONE);
                }
            } else {
                holder.message.setText("");
                holder.time.setText("");
                binding.tvFail.setVisibility(View.GONE);
            }
        }

        setConversationUserInfo(holder, conversationInfo, conversationId);
        if (holder.mentioned.getVisibility() != View.VISIBLE) {
            String unSendMsg = EasePreferenceManager.getInstance().getUnSendMsgInfo(conversationId);
            if (!TextUtils.isEmpty(unSendMsg)) {
                holder.mentioned.setText(R.string.were_not_send_msg);
                holder.message.setText(unSendMsg);
                holder.mentioned.setVisibility(View.VISIBLE);
            }
        }
        binding.shadowLayout.setClickable(!conversationInfo.isGroup());
        if (!conversationInfo.isGroup()) {
            // binding.shadowLayout.setOnClickListener(v -> PersonalPageActivity.lunch(context,conversationId));
        }


        binding.executePendingBindings();
    }

    private void setConversationUserInfo(ViewHolder holder, CustomConversationInfo conversationInfo, String username) {
        holder.mentioned.setVisibility(View.GONE);
        binding.shadowLayout.setCornerRadius(0);
        binding.shadowLayout.setStrokeWidth(0);
        holder.avatar.setShapeType(EaseImageView.ShapeType.ROUND);

        setUserInfo(holder, conversationInfo, username);
    }

    private void bindFinalMessage(ViewHolder holder, Context context, ConversationFinalMessageInfo finalMessage) {
        if (finalMessage != null) {
            binding.vgContent.removeAllViews();
            holder.message.setVisibility(View.VISIBLE);
            holder.time.setText(EaseDateUtils.getTimestampString(context, new Date(finalMessage.getMsgTime())));
            CharSequence content = finalMessage.getContent();
            if (!TextUtils.isEmpty(content)) {
                holder.message.setText(content);
            } else {
                holder.message.setVisibility(View.INVISIBLE);
                EaseCommonUtils.showCustomEmojiView(context, finalMessage.getCustom_msg(), finalMessage.getSend_user_info(), binding.vgContent, finalMessage.getMsgId(), true, false);
            }
        }
    }

    private void setContent(ViewHolder holder, Context context, EMMessage lastMessage, boolean isGroup) {
        EMMessageBody body = lastMessage.getBody();
        binding.vgContent.removeAllViews();
        holder.message.setVisibility(View.VISIBLE);
        holder.time.setText(EaseDateUtils.getTimestampString(context, new Date(lastMessage.getMsgTime())));
        if (body instanceof EMCustomMessageBody) {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) body;
            String event = messageBody.event();
            if (event.equals(ImMsgManager.MSG_CUSTOM_EMOJI)) {
                holder.message.setVisibility(View.INVISIBLE);
                EaseCommonUtils.showCustomEmojiView(context, lastMessage, binding.vgContent, true, isGroup);
            } else {
                setMessageContent(holder, context, lastMessage, isGroup);
            }
        } else {
            setMessageContent(holder, context, lastMessage, isGroup);
        }
    }

    @SuppressLint("SetTextI18n")
    private static void setMessageContent(ViewHolder holder, Context context, EMMessage lastMessage, boolean isGroup) {
        Spannable smiledText = EaseSmileUtils.getSmiledText(context, EaseCommonUtils.getMessageDigest(lastMessage, context));
        holder.message.setText(smiledText);
    }

    private void setUserInfo(ViewHolder holder, CustomConversationInfo conversationInfo, String conversationId) {
        UserDetailInfo dataBean = conversationInfo.getDataBean();
        if (dataBean == null) {
            EMConversation conversation = EMClient.getInstance().chatManager().getConversation(conversationId);
            EMMessage latestMessageFromOthers = conversation.getLatestMessageFromOthers();
            if (latestMessageFromOthers != null) {
                String stringAttribute =
                        latestMessageFromOthers.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "");
                if (!TextUtils.isEmpty(stringAttribute)) {
                    dataBean = GsonUtils.fromJson(
                            stringAttribute, UserDetailInfo.class
                    );
                }
            }
        }

        if (dataBean != null) {
            if (dataBean.isAuth()) {
                binding.ivAuth.setVisibility(View.VISIBLE);
            }else {
                binding.ivAuth.setVisibility(View.GONE);
            }
            if (dataBean.isFriend()) {
                binding.tvTagFriend.setVisibility(View.VISIBLE);
            }else {
                binding.tvTagFriend.setVisibility(View.GONE);
            }
            holder.name.setText(dataBean.getNickName());
            if (!TextUtils.isEmpty(dataBean.getPortrait())) {
                Glide.with(holder.mContext).load(ImageThumbUtils.getThumbUrl(dataBean.getPortrait())).centerCrop().placeholder(R.drawable.ease_default_avatar).error(R.drawable.ease_default_avatar).into(holder.avatar);
            }
            if (dataBean.getOnlineStatus() == 0) {
                binding.onlineIcon.setVisibility(View.VISIBLE);
            } else {
                binding.onlineIcon.setVisibility(View.INVISIBLE);
            }
        } else {
            binding.ivAuth.setVisibility(View.GONE);
            binding.tvTagFriend.setVisibility(View.GONE);
            EaseUserProfileProvider userProvider = EaseIM.getInstance().getUserProvider();
            if (userProvider != null) {
                EaseUser user = userProvider.getUser(conversationId);
                if (user != null) {
                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Drawable drawable = holder.avatar.getDrawable();
                        Glide.with(holder.mContext)
                                .load(user.getAvatar())
                                .error(drawable)
                                .into(holder.avatar);
                    }
                    holder.name.setText(user.getNickname());
                } else {
                    String showName = EaseUserUtils.getDisplayName(conversationId);
                    holder.avatar.setImageResource(R.drawable.ease_default_avatar);
                    holder.name.setText(showName);
                }
            }
        }
    }
}

