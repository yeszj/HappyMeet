package cn.yanhu.imchat.custom.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.manager.EaseMessageTypeSetManager;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.modules.chat.interfaces.IRecyclerViewHandle;
import com.hyphenate.easeui.modules.chat.model.EaseChatItemStyleHelper;
import com.hyphenate.easeui.modules.chat.presenter.EaseChatMessagePresenter;
import com.hyphenate.easeui.modules.chat.presenter.EaseChatMessagePresenterImpl;
import com.hyphenate.easeui.modules.chat.presenter.IChatMessageListView;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.util.EMLog;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.commonres.config.EventBusKeyConfig;
import cn.yanhu.commonres.config.ImMessageParamsConfig;
import cn.yanhu.commonres.manager.AppCacheManager;
import cn.yanhu.imchat.bean.request.RewardRequest;
import cn.yanhu.imchat.manager.EmMsgManager;


public class CustomEaseChatMessageListLayout extends RelativeLayout implements IChatMessageListView, IRecyclerViewHandle
        , CustomIChatMessageItemSet, CustomIChatMessageListLayout {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private EaseChatMessagePresenter presenter;
    private EaseMessageAdapter messageAdapter;
    private ConcatAdapter baseAdapter;
    /**
     * 加载数据的方式，目前有三种，常规模式（从本地加载），漫游模式，查询历史消息模式（通过数据库搜索）
     */
    private LoadDataType loadDataType;
    /**
     * 消息id，一般是搜索历史消息时会用到这个参数
     */
    private String msgId;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private RecyclerView rvList;
    private SwipeRefreshLayout srlRefresh;
    public LinearLayoutManager layoutManager;
    private EMConversation conversation;
    /**
     * 会话类型，包含单聊，群聊和聊天室
     */
    private EMConversation.EMConversationType conType;
    /**
     * 另一侧的环信id
     */
    private String username;
    private boolean canUseRefresh = true;
    private LoadMoreStatus loadMoreStatus;
    private OnMessageTouchListener messageTouchListener;
    private OnChatErrorListener errorListener;
    /**
     * 上一次控件的高度
     */
    private int recyclerViewLastHeight;
    /**
     * 条目具体控件的点击事件
     */
    private MessageListItemClickListener messageListItemClickListener;
    private final EaseChatItemStyleHelper chatSetHelper;

    public CustomEaseChatMessageListLayout(@NonNull Context context) {
        this(context, null);
    }

    public CustomEaseChatMessageListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomEaseChatMessageListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ease_chat_message_list, this);
        EaseChatItemStyleHelper.getInstance().clear();
        chatSetHelper = EaseChatItemStyleHelper.getInstance();
        presenter = new EaseChatMessagePresenterImpl();
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(presenter);
        }
        initAttrs(context, attrs);
        initViews();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EaseChatMessageListLayout);
            float textSize = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_text_size
                    , 0);
            chatSetHelper.setTextSize((int) textSize);
            int textColorRes = a.getResourceId(R.styleable.EaseChatMessageListLayout_ease_chat_item_text_color, -1);
            int textColor;
            if (textColorRes != -1) {
                textColor = ContextCompat.getColor(context, textColorRes);
            } else {
                textColor = a.getColor(R.styleable.EaseChatMessageListLayout_ease_chat_item_text_color, 0);
            }
            chatSetHelper.setTextColor(textColor);

            float itemMinHeight = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_min_height, 0);
            chatSetHelper.setItemMinHeight((int) itemMinHeight);

            float timeTextSize = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_text_size, 0);
            chatSetHelper.setTimeTextSize((int) timeTextSize);
            int timeTextColorRes = a.getResourceId(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_text_color, -1);
            int timeTextColor;
            if (timeTextColorRes != -1) {
                timeTextColor = ContextCompat.getColor(context, textColorRes);
            } else {
                timeTextColor = a.getColor(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_text_color, 0);
            }
            chatSetHelper.setTimeTextColor(timeTextColor);
            chatSetHelper.setTimeBgDrawable(a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_background));

            Drawable avatarDefaultDrawable = a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_default_src);
            //float avatarSize = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_size, 0);
            int shapeType = a.getInteger(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_shape_type, 0);
            //float avatarRadius = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_radius, 0);
            //float borderWidth = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_border_width, 0);
            //int borderColorRes = a.getResourceId(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_border_color, -1);
//            int borderColor;
//            if(borderColorRes != -1) {
//                borderColor = ContextCompat.getColor(context, borderColorRes);
//            }else {
//                borderColor = a.getColor(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_border_color, Color.TRANSPARENT);
//            }
            chatSetHelper.setAvatarDefaultSrc(avatarDefaultDrawable);
//            chatSetHelper.setAvatarSize(avatarSize);
            chatSetHelper.setShapeType(shapeType);
//            chatSetHelper.setAvatarRadius(avatarRadius);
//            chatSetHelper.setBorderWidth(borderWidth);
//            chatSetHelper.setBorderColor(borderColor);

            chatSetHelper.setReceiverBgDrawable(a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_receiver_background));
            chatSetHelper.setSenderBgDrawable(a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_sender_background));

            //chatSetHelper.setShowAvatar(a.getBoolean(R.styleable.EaseChatMessageListLayout_ease_chat_item_show_avatar, true));
            chatSetHelper.setShowNickname(a.getBoolean(R.styleable.EaseChatMessageListLayout_ease_chat_item_show_nickname, false));

            chatSetHelper.setItemShowType(a.getInteger(R.styleable.EaseChatMessageListLayout_ease_chat_item_show_type, 0));

            a.recycle();
        }
    }

    private void initViews() {
        presenter.attachView(this);

        rvList = findViewById(R.id.message_list);
        srlRefresh = findViewById(R.id.srl_refresh);

        srlRefresh.setEnabled(canUseRefresh);

        layoutManager = new LinearLayoutManager(getContext());
        rvList.setLayoutManager(layoutManager);

        baseAdapter = new ConcatAdapter();
        messageAdapter = new EaseMessageAdapter();
        baseAdapter.addAdapter(messageAdapter);
        rvList.setAdapter(baseAdapter);
        registerChatType();

        initListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        if(conversation != null) {
//            conversation.markAllMessagesAsRead();
//        }
        EaseChatItemStyleHelper.getInstance().clear();
        EaseMessageTypeSetManager.getInstance().release();
    }

    private void registerChatType() {
        try {
            EaseMessageTypeSetManager.getInstance().registerMessageType(messageAdapter);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void init(LoadDataType loadDataType, String username, int chatType) {
        this.username = username;
        this.loadDataType = loadDataType;
        this.conType = EaseCommonUtils.getConversationType(chatType);
        try {
            conversation = EMClient.getInstance().chatManager().getConversation(username, conType, true);
            presenter.setupWithConversation(conversation);
        } catch (NullPointerException e) {
        }
    }

    public void init(String username, int chatType) {
        init(LoadDataType.LOCAL, username, chatType);
    }

    public void loadDefaultData() {
        loadData(pageSize, null);
    }

    public void loadData(String msgId) {
        loadData(pageSize, msgId);
    }

    public void loadData(int pageSize, String msgId) {
        this.pageSize = pageSize;
        this.msgId = msgId;
        checkConType();
    }

    private void checkConType() {
        if (isChatRoomCon()) {
            presenter.joinChatRoom(username);
        } else {
            loadData();
        }
    }

    private void loadData() {
        if (!isSingleChat()) {
            chatSetHelper.setShowNickname(true);
        }
        EMLog.e("EMUnRead", "进入聊天界面，消息全部设为已读，会话ID：" + conversation.conversationId() + " 未读消息数：" + conversation.getUnreadMsgCount() + "会话类型：" + conversation.getType());

        conversation.markAllMessagesAsRead();
        if (loadDataType == LoadDataType.ROAM) {
            presenter.loadServerMessages(pageSize);
        } else if (loadDataType == LoadDataType.HISTORY) {
            presenter.loadMoreLocalHistoryMessages(msgId, pageSize, EMConversation.EMSearchDirection.DOWN);
        } else {
            presenter.loadLocalMessages(pageSize);
        }
    }

    /**
     * 加载更多的更早一些的数据，下拉加载更多
     */
    public void loadMorePreviousData() {
        String msgId = getListFirstMessageId();
        if (loadDataType == LoadDataType.ROAM) {
            presenter.loadMoreServerMessages(msgId, pageSize);
        } else if (loadDataType == LoadDataType.HISTORY) {
            presenter.loadMoreLocalHistoryMessages(msgId, pageSize, EMConversation.EMSearchDirection.UP);
        } else {
            presenter.loadMoreLocalMessages(msgId, pageSize, false);
        }
    }

    /**
     * 加载更多的更早一些的数据，下拉加载指定条数
     */
    public void loadMorePreviousData(int pageSize, EMCallBack callBack) {
        this.pageSize = pageSize;
        String msgId = getListFirstMessageId();
        presenter.loadMoreLocalMessages(msgId, pageSize, false, callBack);
    }

    /**
     * 专用于加载更多的更新一些的数据，上拉加载更多时使用
     */
    public void loadMoreHistoryData() {
        String msgId = getListLastMessageId();
        if (loadDataType == LoadDataType.HISTORY) {
            loadMoreStatus = LoadMoreStatus.HAS_MORE;
            presenter.loadMoreLocalHistoryMessages(msgId, pageSize, EMConversation.EMSearchDirection.DOWN);
        }
    }

    /**
     * 获取列表最下面的一条消息的id
     */
    private String getListFirstMessageId() {
        EMMessage message = null;
        try {
            message = messageAdapter.getData().get(layoutManager.getReverseLayout() ? messageAdapter.getData().size() - 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message == null ? null : message.getMsgId();
    }

    /**
     * 获取列表最下面的一条消息的id
     *
     * @return
     */
    private String getListLastMessageId() {
        EMMessage message = null;
        try {
            message = messageAdapter.getData().get(layoutManager.getReverseLayout() ? 0 : messageAdapter.getData().size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message == null ? null : message.getMsgId();
    }

    public boolean isChatRoomCon() {
        return conType == EMConversation.EMConversationType.ChatRoom;
    }

    public boolean isGroupChat() {
        return conType == EMConversation.EMConversationType.GroupChat;
    }

    private boolean isSingleChat() {
        return conType == EMConversation.EMConversationType.Chat;
    }

    private void initListener() {
        LiveEventBus.get(EventBusKeyConfig.SEND_MESSAGE_SUCCESS).observe((FragmentActivity) getContext(), new Observer<Object>() {
            @Override
            public void onChanged(Object aBoolean) {
                runOnUi(() -> {
                    if (messageAdapter != null) {
                        checkMessageReward(messageAdapter.getData(), true);
                    }
                });

            }
        });
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMorePreviousData();
            }
        });
        rvList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //判断状态及是否还有更多数据
                    if (loadDataType == LoadDataType.HISTORY
                            && loadMoreStatus == LoadMoreStatus.HAS_MORE
                            && layoutManager.findLastVisibleItemPosition() != 0
                            && layoutManager.findLastVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                        //加载更多
                        loadMoreHistoryData();
                    }
                } else {
                    //if recyclerView not idle should hide keyboard
                    if (messageTouchListener != null) {
                        messageTouchListener.onViewDragging();
                    }
                }
            }
        });

        //用于监听RecyclerView高度的变化，从而刷新列表
        rvList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = rvList.getHeight();
                if (recyclerViewLastHeight == 0) {
                    recyclerViewLastHeight = height;
                }
                if (recyclerViewLastHeight != height) {
                    //RecyclerView高度发生变化，刷新页面
                    if (messageAdapter.getData() != null && !messageAdapter.getData().isEmpty()) {
                        post(() -> seekToPosition(messageAdapter.getData().size() - 1));
                    }
                }
                recyclerViewLastHeight = height;
            }
        });

        messageAdapter.setOnItemClickListener((view, position) -> {
            if (messageTouchListener != null) {
                messageTouchListener.onTouchItemOutside(view, position);
            }
        });
        messageAdapter.setListItemClickListener(new MessageListItemClickListener() {
            @Override
            public boolean onBubbleClick(EMMessage message) {
                if (messageListItemClickListener != null) {
                    return messageListItemClickListener.onBubbleClick(message);
                }
                return false;
            }

            @Override
            public boolean onResendClick(EMMessage message) {
                if (messageListItemClickListener != null) {
                    return messageListItemClickListener.onResendClick(message);
                }
                return false;
            }

            @Override
            public boolean onBubbleLongClick(View v, EMMessage message) {
                if (messageListItemClickListener != null) {
                    return messageListItemClickListener.onBubbleLongClick(v, message);
                }
                return false;
            }

            @Override
            public void onQuoteViewClick(EMMessage message) {
                EMLog.e("apex", "onQuoteViewClick1: " + message);
                if (messageListItemClickListener != null) {
                    messageListItemClickListener.onQuoteViewClick(message);
                }
            }

            @Override
            public boolean onQuoteViewLongClick(View v, EMMessage message) {
                if (messageListItemClickListener != null) {
                    return messageListItemClickListener.onQuoteViewLongClick(v, message);
                }
                return false;
            }

            @Override
            public void onUserAvatarClick(String username) {
                if (messageListItemClickListener != null) {
                    messageListItemClickListener.onUserAvatarClick(username);
                }
            }

            @Override
            public void onUserAvatarLongClick(String username) {
                if (messageListItemClickListener != null) {
                    messageListItemClickListener.onUserAvatarLongClick(username);
                }
            }

            @Override
            public void onMessageCreate(EMMessage message) {
                if (messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageCreate(message);
                }
            }

            @Override
            public void onMessageSuccess(EMMessage message) {
                if (messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageSuccess(message);
                }
            }

            @Override
            public void onMessageError(EMMessage message, int code, String error) {
                if (messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageError(message, code, error);
                }
            }

            @Override
            public void onMessageInProgress(EMMessage message, int progress) {
                if (messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageInProgress(message, progress);
                }
            }
        });
    }

    /**
     * 停止下拉动画
     */
    private void finishRefresh() {
        if (presenter.isActive()) {
            runOnUi(() -> {
                if (srlRefresh != null) {
                    srlRefresh.setRefreshing(false);
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void notifyDataSetChanged() {
        messageAdapter.notifyDataSetChanged();
    }


    private int msgSize = 0;
    private long setDataTime = 0;

    public void setData(List<EMMessage> data) {
        if (msgSize == data.size() && System.currentTimeMillis() - setDataTime <= 2000) {
            return;
        }
        setDataTime = System.currentTimeMillis();
        msgSize = data.size();
        if (isSingleChat()) {
            checkMessage(data);
        }
        boolean isReverse = data.size() > 7;
        layoutManager.setReverseLayout(isReverse);
        if (isReverse) {
            CommonUtils.reverseList(data);
        }
        rvList.setLayoutManager(layoutManager);
        messageAdapter.setData(data);
        if (isSingleChat()) {
            checkMessageReward(data, false);
        }
        if (AppCacheManager.isWoman()) {
            if (data.size() == 0 || !hasSendMsg(data)) {
                LiveEventBus.get(EventBusKeyConfig.SHOW_FAST_CHAT).post(true);
            } else {
                LiveEventBus.get(EventBusKeyConfig.SHOW_FAST_CHAT).post(false);
            }
        }
    }


    private boolean hasSendMsg(List<EMMessage> data) {
        for (int i = data.size() - 1; i >= 0; i--) {
            EMMessage emMessage = data.get(i);
            if (emMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void checkMessageReward(List<EMMessage> data, boolean isRefresh) {
        if (AppCacheManager.isMan()) {
            return;
        }
        if (data == null || data.size() == 0) {
            return;
        }
        Map<String, Long> msgIdList = new HashMap<>(); //收益状态还是初始状态的消息id
        List<Long> timeList = new ArrayList<>(); //自己发送的消息的时间戳
        for (int i = 0; i < data.size(); i++) {
            EMMessage emMessage = data.get(i);
            if (!Objects.equals(emMessage.getFrom(), AppCacheManager.INSTANCE.getUserId())) {
                //还没发放收益的消息
                addRewardMsgInfo(msgIdList, emMessage);
            } else {
                if (isLoveMsg(emMessage)) {
                    //为爱牵线消息 /还没发放收益的消息
                    addRewardMsgInfo(msgIdList, emMessage);
                } else {
                    if (emMessage.status() == EMMessage.Status.SUCCESS) {
                        //回复的消息时间戳
                        if (EmMsgManager.isEditSendMessage(emMessage)) {
                            timeList.add(emMessage.getMsgTime());
                        }
                    }
                }


            }
        }
        Set<String> keys = msgIdList.keySet();
        Map<String, RewardRequest.RewardInfo> rewardMap = new HashMap<>();
        for (String msgId : keys) {
            //遍历消息奖励还是初始状态的消息
            Long msgTime = msgIdList.get(msgId);
            if (msgTime == null) {
                continue;
            }
            EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
            int rewardStatus = message.getIntAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 0);
            if (rewardStatus != 0) {
                continue;
            }
            boolean hasReply = false;
            for (int i = 0; i < timeList.size(); i++) {
                Long replyTime = timeList.get(i);
                if (replyTime > msgTime) {
                    hasReply = true;
                    //如果自己发送的消息时间有大于还未发放奖励的消息 说明自己已经回复了 可以判断收到的消息奖励是否已经发放
                    long differTime = replyTime - msgTime;
                    boolean isFirstMessage = message.getBooleanAttribute(ImMessageParamsConfig.KEY_IS_FIRST_MESSAGE, false);
                    float rewardGoldSum = message.getFloatAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, 0f);
                    if (rewardGoldSum == 0) {
                        rewardGoldSum = message.getIntAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, 0);
                    }
                    if (EmMsgManager.isQianXianMessage(message)) {
                        //牵线消息
                        if (isInTwoMinute(differTime)) {
                            //2分钟内回复奖励 *200%
                            if (rewardGoldSum > 0) {
                                rewardGoldSum = rewardGoldSum * 2;
                                message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, rewardGoldSum);
                                message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 1);
                                if (!rewardMap.containsKey(msgId)) {
                                    RewardRequest.RewardInfo rewardInfo = new RewardRequest.RewardInfo(message.getMsgId(), String.valueOf(rewardGoldSum), isLoveMsg(message) ? 1 : 2);
                                    rewardMap.put(message.getMsgId(), rewardInfo);

                                }
                            }
                        } else {
                            //未获得奖励
                            message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, -1);
                        }
                    } else {
                        if (differTime > 4 * 60 * 60 * 1000L) {
                            //超过4小时 无收益
                            message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, -1);
                        } else {
                            //未超过4小时 获得收益
                            if (isFirstMessage || isReplyLoveMessage(message)) {
                                int chatSource = message.getIntAttribute(ImMessageParamsConfig.KEY_CHAT_SOURCE, -1);
                                if (chatSource == 0) {
                                    //搭讪消息
                                    if (isInTwoMinute(differTime)) {
                                        //2分钟内回复 收益*120%
                                        rewardGoldSum = rewardGoldSum * 1.2f;
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, rewardGoldSum);
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 1);
                                        if (!rewardMap.containsKey(msgId)) {
                                            RewardRequest.RewardInfo rewardInfo = new RewardRequest.RewardInfo(message.getMsgId(), String.valueOf(rewardGoldSum), isFirstMessage ? 3 : 4);
                                            rewardMap.put(message.getMsgId(), rewardInfo);

                                        }
                                    } else if (isInHalfHour(differTime)) {
                                        //半小时内回复 收益*100%
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 1);
                                        if (!rewardMap.containsKey(msgId)) {
                                            RewardRequest.RewardInfo rewardInfo = new RewardRequest.RewardInfo(message.getMsgId(), String.valueOf(rewardGoldSum), isFirstMessage ? 3 : 4);
                                            rewardMap.put(message.getMsgId(), rewardInfo);

                                        }
                                    } else {
                                        //超过半小时无收益
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, -1);
                                    }
                                } else {
                                    String replyDesc = "";
                                    if (isInTwoMinute(differTime)) {
                                        //2分钟内回复 收益*120%
                                        rewardGoldSum = rewardGoldSum * 1.2f;
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, rewardGoldSum);
                                    } else if (isInfHour(differTime, 1)) {
                                        //1小时内回复 收益*100%
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 1);
                                    } else if (isInfHour(differTime, 2)) {
                                        //2小时内回复 收益*80%
                                        replyDesc = "超时回复扣-20%";
                                        rewardGoldSum = rewardGoldSum * 0.8f;
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, rewardGoldSum);
                                    } else {
                                        //4小时内回复 收益*50%
                                        replyDesc = "超时回复扣-50%";
                                        rewardGoldSum = rewardGoldSum * 0.5f;
                                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, rewardGoldSum);
                                    }
                                    if (!rewardMap.containsKey(msgId)) {
                                        RewardRequest.RewardInfo rewardInfo = new RewardRequest.RewardInfo(message.getMsgId(), String.valueOf(rewardGoldSum), isFirstMessage ? 3 : 4);
                                        rewardMap.put(message.getMsgId(), rewardInfo);

                                    }
                                    message.setAttribute(ImMessageParamsConfig.KEY_REPLY_OUT_TIME_DESC, replyDesc);
                                    message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 1);
                                }
                            } else {
                                message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 1);
                            }

                        }
                    }

                    EmMsgManager.updateMsg(message);
                    if (isRefresh) {
                        notifyDataSetChanged();
                    }
                    break;
                }
            }
            if (!hasReply) {
                long differTime = System.currentTimeMillis() - message.getMsgTime();
                if (EmMsgManager.isQianXianMessage(message)) {
                    //牵线消息
                    if (!isInTwoMinute(differTime)) {
                        //未获得奖励
                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, -1);
                        EmMsgManager.updateMsg(message);
                    }
                } else {
                    if (differTime > 4 * 60 * 60 * 1000L) {
                        //超过4小时 无收益
                        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, -1);
                        EmMsgManager.updateMsg(message);
                    }
                }
            }
        }
        if (rewardMap.size() > 0) {
            Collection<RewardRequest.RewardInfo> values = rewardMap.values();
            RewardRequest request = new RewardRequest(conversation.conversationId(), new ArrayList<>(values));
            EmMsgManager.receiveChatReward(request);
        }

    }

    private static void addRewardMsgInfo(Map<String, Long> msgIdList, EMMessage emMessage) {
        int rewardGoldSum = emMessage.getIntAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, 0);
        int rewardGoldStatus = emMessage.getIntAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 0);
        if (rewardGoldStatus == 0 && rewardGoldSum > 0) {
            msgIdList.put(emMessage.getMsgId(), emMessage.getMsgTime());
        }
    }

    private static boolean isInfHour(long differTime, int hour) {
        return differTime < hour * 60 * 60 * 1000L;
    }

    private static boolean isInHalfHour(long differTime) {
        return differTime < 30 * 60 * 1000;
    }

    private static boolean isInTwoMinute(long differTime) {
        return differTime < 2 * 60 * 1000;
    }


    public int replyQianXianCount = -1; //女生回复缘分牵线消息的数量用于控制缘分牵线后女生最多回三句
    public int replyCount = 0; //女生回复消息的数量 -1代表女生回复的三条消息内 对方已经回复过了

    /**
     * 过滤掉系统发送的牵线消息
     */
    private void checkMessage(List<EMMessage> data) {
        replyCount = 0;
        List<Long> replyTimeList = getReplyTime(data); //获取所有对方发送的消息时间戳
        checkReplyFateQianXianCount(data, replyTimeList);

        for (int i = data.size() - 1; i >= 0; i--) {
            EMMessage emMessage = data.get(i);
            if (AppCacheManager.isWoman() && replyCount != -1) {
                if (emMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                    if (EmMsgManager.isEditSendMessage(emMessage)) {
                        replyCount++;
                    }
                } else {
                    if (replyCount < 3) {
                        replyCount = -1;
                    }
                }
            }
            if (emMessage.getType() == EMMessage.Type.CUSTOM) {
                EMCustomMessageBody messageBody = (EMCustomMessageBody) emMessage.getBody();
                String event = messageBody.event();
                if (ChatConstant.MSG_QIANXIAN.equals(event)) {
                    Map<String, String> params = messageBody.getParams();
                    String content = params.get("content");
                    if (!TextUtils.isEmpty(content) && (content.startsWith("为爱牵线") || content.startsWith("缘分牵线"))) {
                        EMConversation conversation =
                                EMClient.getInstance().chatManager().getConversation(emMessage.conversationId());
                        if (conversation != null) {
                            if (AppCacheManager.isMan()) {
                                //男方牵线系统消息不显示
                                if (content.startsWith("缘分牵线")) {
                                    data.remove(emMessage);
                                }
                                continue;
                            }

                            if (replyTimeList.size() > 0) {
                                long replyTime = isReply(emMessage, replyTimeList);
                                if (replyTime != -1) {
                                    //已经回复过
//                                    if (isLoveMsg(emMessage) && emMessage.getFrom().equals(BaseApplication.getUserId())) {
//                                        //（2024-09-12去掉此逻辑了）如果男生回复了为爱牵线 更新女方为爱牵线系统消息的时间为男生回复的时间(因为为爱牵线是男方回复后女方才能看到)
//                                        emMessage.setMsgTime(replyTime - 1);
//                                    }
                                    dealMessageWhenHasAnswer(data, emMessage, content);
                                } else {
                                    dealMessageWhenNoAnswer(data, emMessage, content);
                                }
                            } else {
                                //男生未回复过 计算女生发送消息的数量,用于发送消息时控制缘分牵线后女生最多回三句
                                dealMessageWhenNoAnswer(data, emMessage, content);
                            }
                        } else {
                            break;
                        }
                    }
                }
            } else if (emMessage.getIntAttribute("loveString", -1) == 1) {
                /** V2.2.4版本逻辑修改 女方回复后不显示改成显示
                 * 处理牵线时系统自动发的文本消息
                 * 为爱牵线  发送方未回复时(女方)不显示，回复后显示，接收方(男方)显示
                 */
                if (emMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                    long replyTime = isReply(emMessage, replyTimeList);
                    if (replyTime == -1) {
                        //未回复时 删除为爱牵线的文本消息 已回复后-V2.2.4版本改成显示牵线文本消息
                        data.remove(emMessage);
                    }
                }

            } else {
                if (!emMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                }
            }
        }
    }

    private void checkReplyFateQianXianCount(List<EMMessage> data, List<Long> replyTimeList) {
        if (AppCacheManager.isWoman()) {
            for (int i = data.size() - 1; i >= 0; i--) {
                EMMessage emMessage = data.get(i);
                if (emMessage.getType() == EMMessage.Type.CUSTOM) {
                    if (EmMsgManager.isFateMessage(emMessage)) {
                        if (replyTimeList.size() > 0) {
                            long replyTime = isReply(emMessage, replyTimeList);
                            //女生收到的缘分牵线后 男生是否主动发送过消息给女生
                            if (replyTime != -1) {
                                //男生回复过 女生发送的消息数量重新计数
                                replyQianXianCount = -1;
                            } else {
                                //男生未回复过 计算女生发送消息的数量,用于发送消息时控制缘分牵线后女生最多回三句
                                replyQianXianCount = getReplyFateQianXianCount(data, emMessage.getMsgTime());
                            }
                        } else {
                            //男生未回复过 计算女生发送消息的数量,用于发送消息时控制缘分牵线后女生最多回三句
                            replyQianXianCount = getReplyFateQianXianCount(data, emMessage.getMsgTime());
                        }
                        break;
                    }
                }
            }
        }
    }


    /**
     * 用于判断消息是否被回复
     *
     * @param emMessage 消息实体 判断的就是此条消息是否已经被回复过
     * @param replyTime 接收到的消息时间戳
     * @return 回复消息的时间戳
     */
    private static long isReply(EMMessage emMessage, List<Long> replyTime) {
        for (int j = 0; j < replyTime.size(); j++) {
            if (replyTime.get(j) >= emMessage.getMsgTime()) {
                return replyTime.get(j);
            }
        }
        return -1;
    }

    /**
     * 获取回复缘分牵线消息的数量
     *
     * @return
     */
    private int getReplyFateQianXianCount(List<EMMessage> data, long qianxianMsgTime) {
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            EMMessage emMessage = data.get(i);
            if (Objects.equals(emMessage.getFrom(), AppCacheManager.INSTANCE.getUserId())) {
                EMMessage.Type type = emMessage.getType();
                if (type == EMMessage.Type.CUSTOM) {
                    //自定义消息除了gif动图消息和emoji和文本混合消息 其它消息不算回复消息
                    EMCustomMessageBody body = (EMCustomMessageBody) emMessage.getBody();
                    String event = body.event();
                    if (event.equals(ChatConstant.MSG_CUSTOM_EMOJI) || event.equals(ChatConstant.MSG_CUSTOM_GIF_EMOJI)) {
                        if (emMessage.getMsgTime() > qianxianMsgTime) {
                            count++;
                        }
                    }
                } else {
                    if (emMessage.getMsgTime() > qianxianMsgTime && !EmMsgManager.isLoveSendMessage(emMessage)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private List<Long> getReplyTime(List<EMMessage> data) {
        List<Long> timeList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            EMMessage emMessage = data.get(i);
            if (!Objects.equals(emMessage.getFrom(), AppCacheManager.INSTANCE.getUserId())) {
                if (emMessage.status() == EMMessage.Status.SUCCESS) {
                    EMMessage.Type type = emMessage.getType();
                    if (type == EMMessage.Type.CUSTOM) {
                        //自定义消息除了gif动图消息和emoji和文本混合消息 其它消息不算回复消息
                        EMCustomMessageBody body = (EMCustomMessageBody) emMessage.getBody();
                        String event = body.event();
                        if (event.equals(ChatConstant.MSG_CUSTOM_EMOJI) || event.equals(ChatConstant.MSG_CUSTOM_GIF_EMOJI)) {
                            timeList.add(emMessage.getMsgTime());
                        }
                    } else {
                        timeList.add(emMessage.getMsgTime());
                    }
                }
            }
        }
        return timeList;
    }


    private static boolean isLoveMsg(EMMessage lastMessage) {
        if (lastMessage.getType() == EMMessage.Type.CUSTOM) {
            EMCustomMessageBody messageBody2 = (EMCustomMessageBody) lastMessage.getBody();
            String event2 = messageBody2.event();
            if (ChatConstant.MSG_QIANXIAN.equals(event2)) {
                Map<String, String> params = messageBody2.getParams();
                if (params.containsKey("content")) {
                    String content = params.get("content");
                    return !TextUtils.isEmpty(content) && content.startsWith("为爱牵线");
                }
            }
        }
        return false;
    }

    /**
     * 是否是男生收到为爱牵线后回复的消息
     *
     * @param lastMessage
     * @return
     */
    private static boolean isReplyLoveMessage(EMMessage lastMessage) {
        return lastMessage.getBooleanAttribute(ImMessageParamsConfig.KEY_IS_REPLY_LOVE_MSG, false);
    }


    /**
     * 牵线消息已回复 处理逻辑
     * 1.为爱牵线(系统女方给男方发)
     * 女方显示为爱牵线 + 文本消息(V2.2.4版本改成显示牵线文本消息)
     * 男方显示对方消息和为爱牵线系统消息
     * 2.缘分牵线(系统男方给女方发)
     * 女方显示缘分牵线
     */
    private void dealMessageWhenHasAnswer(List<EMMessage> data, EMMessage emMessage, String content) {
        if (!TextUtils.isEmpty(content)) {
            if (content.startsWith("缘分牵线")) {
                //"[缘分牵线]";
                if (emMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                    //发送方不显示
                    data.remove(emMessage);
                }
            }
        }
    }

    /**
     * 牵线消息未回复 处理逻辑
     * 1.为爱牵线(系统女方给男方发)
     * 女方(发送方)不显示为爱牵线+文本消息
     * 男方显示对方消息和为爱牵线
     * 2.缘分牵线(系统男方给女方发)
     * 男方(发送方)不显示缘分牵线
     * 女方显示缘分牵线
     */
    private void dealMessageWhenNoAnswer(List<EMMessage> data, EMMessage emMessage, String content) {
        if (!TextUtils.isEmpty(content)) {
            if (content.startsWith("缘分牵线")) {
                //"[缘分牵线]";
                if (emMessage.getFrom().equals(AppCacheManager.INSTANCE.getUserId())) {
                    //发送方不显示
                    data.remove(emMessage);
                }
            } else if (content.startsWith("为爱牵线")) {
                // "[为爱牵线]";
                if (AppCacheManager.isWoman()) {
                    data.remove(emMessage);
                }
            }
        }

    }

    /**
     * 添加数据
     *
     * @param data
     */
    public void addData(List<EMMessage> data) {
        messageAdapter.addData(data);
    }

    @Override
    public Context context() {
        return getContext();
    }

    @Override
    public EMConversation getCurrentConversation() {
        return conversation;
    }

    @Override
    public void joinChatRoomSuccess(EMChatRoom value) {
        loadData();
    }

    @Override
    public void joinChatRoomFail(int error, String errorMsg) {
        if (presenter.isActive()) {
            runOnUi(() -> {
                if (errorListener != null) {
                    errorListener.onChatError(error, errorMsg);
                }
            });
        }
    }

    @Override
    public void loadMsgFail(int error, String message) {
        finishRefresh();
        if (errorListener != null) {
            errorListener.onChatError(error, message);
        }
    }

    @Override
    public void loadLocalMsgSuccess(List<EMMessage> data) {
        refreshToLatest();
    }

    @Override
    public void loadNoLocalMsg() {

    }

    @Override
    public void loadMoreLocalMsgSuccess(List<EMMessage> data, boolean isJumpFirst) {
        finishRefresh();
        presenter.refreshCurrentConversation();
        if (isJumpFirst) {
            post(() -> seekToPosition(data.size() - 1));
        }
    }

    @Override
    public void loadNoMoreLocalMsg() {
        finishRefresh();
    }

    @Override
    public void loadMoreLocalHistoryMsgSuccess(List<EMMessage> data, EMConversation.EMSearchDirection direction) {
        if (direction == EMConversation.EMSearchDirection.UP) {
            finishRefresh();
            messageAdapter.addData(0, data);
        } else {
            messageAdapter.addData(data);
            if (data.size() >= pageSize) {
                loadMoreStatus = LoadMoreStatus.HAS_MORE;
            } else {
                loadMoreStatus = LoadMoreStatus.NO_MORE_DATA;
            }
        }
    }

    @Override
    public void loadNoMoreLocalHistoryMsg() {
        finishRefresh();
    }

    @Override
    public void loadServerMsgSuccess(List<EMMessage> data) {
        refreshToLatest();
    }

    @Override
    public void loadMoreServerMsgSuccess(List<EMMessage> data) {
        finishRefresh();
        presenter.refreshCurrentConversation();
        // post(() -> seekToPosition(data.size() - 1));
    }

    @Override
    public void refreshCurrentConSuccess(List<EMMessage> data, boolean toLatest) {

        setData(data);
        if (toLatest) {
//            int position = messageAdapter.getData().size() - 1;
//            rvList.post(() -> {
//                if (position > 0) {
//                    rvList.smoothScrollToPosition(position);
//                }
//            });
            seekToPosition(messageAdapter.getData().size() - 1);
        }
    }

    @Override
    public void canUseDefaultRefresh(boolean canUseRefresh) {
        this.canUseRefresh = canUseRefresh;
        srlRefresh.setEnabled(canUseRefresh);
    }

    @Override
    public void refreshMessages() {
        if (conversation != null) {
            EMLog.e("EMUnRead", "refreshMessages，消息全部设为已读，会话ID：" + conversation.conversationId() + " 未读消息数：" + conversation.getUnreadMsgCount() + "会话类型：" + conversation.getType());
            presenter.refreshCurrentConversation();
        }
    }

    @Override
    public void refreshToLatest() {
        if (conversation != null) {
            EMLog.e("EMUnRead", "refreshToLatest，消息全部设为已读，会话ID：" + conversation.conversationId() + " 未读消息数：" + conversation.getUnreadMsgCount() + "会话类型：" + conversation.getType());
            presenter.refreshToLatest();
        }
    }

    @Override
    public void refreshMessage(EMMessage message) {
        int position = messageAdapter.getData().lastIndexOf(message);
        if (position != -1) {
            runOnUi(() -> messageAdapter.notifyItemChanged(position));
        }
    }

    @Override
    public void removeMessage(EMMessage message) {
        if (message == null || messageAdapter.getData() == null) {
            return;
        }
        conversation.removeMessage(message.getMsgId());
        EMClient.getInstance().translationManager().removeTranslationResult(message.getMsgId());
        runOnUi(() -> {
            if (presenter.isActive()) {
                List<EMMessage> messages = messageAdapter.getData();
                int position = messages.lastIndexOf(message);
                if (position != -1) {
                    //需要保证条目从集合中删除
                    messages.remove(position);
                    //通知适配器删除条目
                    messageAdapter.notifyItemRemoved(position);
                    //通知刷新下一条消息
                    messageAdapter.notifyItemChanged(position);
                }
            }
        });
    }

    @Override
    public void moveToPosition(int position) {
        seekToPosition(position);
    }

    @Override
    public void lastMsgScrollToBottom(EMMessage message) {
        List<EMMessage> messages = messageAdapter.getData();
        int position = messages.lastIndexOf(message);
        if (position != -1) {
            messageAdapter.notifyItemChanged(position);
            boolean isNoBottom = rvList.canScrollVertically(1);
            if (!isNoBottom) {
                View oldView = rvList.getLayoutManager().findViewByPosition(messageAdapter.getItemCount() - 1);
                int oldHeight = 0;
                if (oldView != null) {
                    oldHeight = oldView.getMeasuredHeight();
                }
                int finalOldHeight = oldHeight;
                rvList.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View v = rvList.getLayoutManager().findViewByPosition(messageAdapter.getItemCount() - 1);
                        int height = 0;
                        if (v != null) {
                            height = v.getMeasuredHeight();
                        }
                        rvList.smoothScrollBy(0, height - finalOldHeight);
                    }
                }, 500);
            }
        }
    }

    @Override
    public void highlightItem(int position) {
        runOnUi(() -> {
            if (messageAdapter != null) {
                messageAdapter.highlightItem(position);
            }
        });
    }

    @Override
    public void showNickname(boolean showNickname) {
        chatSetHelper.setShowNickname(showNickname);
        notifyDataSetChanged();
    }

    @Override
    public void setItemSenderBackground(Drawable bgDrawable) {
        chatSetHelper.setSenderBgDrawable(bgDrawable);
        notifyDataSetChanged();
    }

    @Override
    public void setItemReceiverBackground(Drawable bgDrawable) {
        chatSetHelper.setReceiverBgDrawable(bgDrawable);
        notifyDataSetChanged();
    }

    @Override
    public void setItemTextSize(int textSize) {
        chatSetHelper.setTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setItemTextColor(int textColor) {
        chatSetHelper.setTextColor(textColor);
        notifyDataSetChanged();
    }

//    @Override
//    public void setItemMinHeight(int height) {
//        chatSetHelper.setItemMinHeight(height);
//        notifyDataSetChanged();
//    }

    @Override
    public void setTimeTextSize(int textSize) {
        chatSetHelper.setTimeTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setTimeTextColor(int textColor) {
        chatSetHelper.setTimeTextColor(textColor);
        notifyDataSetChanged();
    }

    @Override
    public void setTimeBackground(Drawable bgDrawable) {
        chatSetHelper.setTimeBgDrawable(bgDrawable);
        notifyDataSetChanged();
    }

    @Override
    public void setItemShowType(ShowType type) {
        if (!isSingleChat()) {
            chatSetHelper.setItemShowType(type.ordinal());
            notifyDataSetChanged();
        }
    }

    @Override
    public void setAvatarDefaultSrc(Drawable src) {
        chatSetHelper.setAvatarDefaultSrc(src);
        notifyDataSetChanged();
    }

//    @Override
//    public void setAvatarSize(float avatarSize) {
//        chatSetHelper.setAvatarSize(avatarSize);
//        notifyDataSetChanged();
//    }

    @Override
    public void setAvatarShapeType(int shapeType) {
        chatSetHelper.setShapeType(shapeType);
        notifyDataSetChanged();
    }

//    @Override
//    public void setAvatarRadius(int radius) {
//        chatSetHelper.setAvatarRadius(radius);
//        notifyDataSetChanged();
//    }

//    @Override
//    public void setAvatarBorderWidth(int borderWidth) {
//        chatSetHelper.setBorderWidth(borderWidth);
//        notifyDataSetChanged();
//    }

//    @Override
//    public void setAvatarBorderColor(int borderColor) {
//        chatSetHelper.setBorderColor(borderColor);
//        notifyDataSetChanged();
//    }

    @Override
    public void addHeaderAdapter(RecyclerView.Adapter adapter) {
        baseAdapter.addAdapter(0, adapter);
    }

    @Override
    public void addFooterAdapter(RecyclerView.Adapter adapter) {
        baseAdapter.addAdapter(adapter);
    }

    @Override
    public void removeAdapter(RecyclerView.Adapter adapter) {
        baseAdapter.removeAdapter(adapter);
    }

    @Override
    public void addRVItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        rvList.addItemDecoration(decor);
    }

    @Override
    public void removeRVItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        rvList.removeItemDecoration(decor);
    }


    /**
     * 移动到指定位置
     *
     * @param position
     */
    private void seekToPosition(int position) {
        if (presenter.isDestroy() || rvList == null) {
            return;
        }
        if (position < 0 || messageAdapter.getData().size() > 5) {
            position = 0;
        }
        RecyclerView.LayoutManager manager = rvList.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
        }
    }

    @Override
    public void setPresenter(EaseChatMessagePresenter presenter) {
        this.presenter = presenter;
        if (getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getLifecycle().addObserver(presenter);
        }
        this.presenter.attachView(this);
        this.presenter.setupWithConversation(conversation);
    }

    @Override
    public EaseMessageAdapter getMessageAdapter() {
        return messageAdapter;
    }

    @Override
    public RecyclerView getListView() {
        return rvList;
    }

    @Override
    public void setOnMessageTouchListener(OnMessageTouchListener listener) {
        this.messageTouchListener = listener;
    }

    @Override
    public void setOnChatErrorListener(OnChatErrorListener listener) {
        this.errorListener = listener;
    }

    @Override
    public void setMessageListItemClickListener(MessageListItemClickListener listener) {
        this.messageListItemClickListener = listener;
    }


    public void runOnUi(Runnable runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable);
    }

    /**
     * 消息列表接口
     */
    public interface OnMessageTouchListener {
        /**
         * touch事件
         *
         * @param v
         * @param position
         */
        void onTouchItemOutside(View v, int position);

        /**
         * 控件正在被拖拽
         */
        void onViewDragging();
    }

    public interface OnChatErrorListener {
        /**
         * 聊天中错误信息
         *
         * @param code
         * @param errorMsg
         */
        void onChatError(int code, String errorMsg);
    }

    /**
     * 三种数据加载模式，local是从本地数据库加载，Roam是开启消息漫游，History是搜索本地消息
     */
    public enum LoadDataType {
        LOCAL, ROAM, HISTORY
    }

    /**
     * 加载更多的状态
     */
    public enum LoadMoreStatus {
        IS_LOADING, HAS_MORE, NO_MORE_DATA
    }

    /**
     * 条目的展示方式
     * normal区分发送方和接收方
     * left发送方和接收方在左侧
     * right发送方和接收方在右侧
     */
    public enum ShowType {
        NORMAL, LEFT/*, RIGHT*/
    }
}

