package cn.yanhu.imchat.ui.conversation;


import static cn.yanhu.imchat.ui.conversation.IMConversationListFrg.TYPE_ALL;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.modules.conversation.interfaces.OnConversationChangeListener;
import com.hyphenate.easeui.modules.conversation.interfaces.OnConversationLoadListener;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.modules.menu.EasePopupMenuHelper;
import com.hyphenate.easeui.modules.menu.OnPopupMenuItemClickListener;
import com.hyphenate.easeui.modules.menu.OnPopupMenuPreShowListener;
import com.hyphenate.easeui.ui.base.EaseBaseFragment;
import com.hyphenate.util.EMLog;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;

import cn.yanhu.commonres.config.IntentKeyConfig;
import cn.yanhu.imchat.custom.chat.CustomEaseConversationListLayout;


public class CustomEaseConversationListFragment extends EaseBaseFragment implements OnItemClickListener, OnPopupMenuItemClickListener, OnPopupMenuPreShowListener, OnRefreshListener, OnConversationLoadListener, OnConversationChangeListener {
    private static final String TAG = CustomEaseConversationListFragment.class.getSimpleName();
    public LinearLayout llRoot;
    public CustomEaseConversationListLayout conversationListLayout;
    public SmartRefreshLayout srlRefresh;
    protected int type = TYPE_ALL;
    protected boolean isIntimacy = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(savedInstanceState);
        initListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    public int getLayoutId() {
        return R.layout.ease_fragment_conversations;
    }


    public void initView(Bundle savedInstanceState) {
        llRoot = findViewById(R.id.ll_root);
        srlRefresh = findViewById(R.id.srl_refresh);
        conversationListLayout = findViewById(R.id.list_conversation);
        type = requireArguments().getInt(IntentKeyConfig.TYPE);
        isIntimacy = type == IMConversationListFrg.TYPE_INTIMACY_ALL || type == IMConversationListFrg.TYPE_SEVEN_DAY_IN;
        conversationListLayout.init(type);

        conversationListLayout.rvConversationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(mContext).resumeRequests();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) < 150) {
                    Glide.with(mContext).resumeRequests();
                } else {
                    Glide.with(mContext).pauseRequests();
                }
            }
        });
        conversationListLayout.setContentTextColor(Color.parseColor("#CC333333"));
    }



    public void initListener() {
        conversationListLayout.setOnItemClickListener(this);
        conversationListLayout.setOnPopupMenuItemClickListener(this);
        conversationListLayout.setOnPopupMenuPreShowListener(this);
        conversationListLayout.setOnConversationLoadListener(this);
        conversationListLayout.setOnConversationChangeListener(this);
        srlRefresh.setOnRefreshListener(this);
    }

    public void initData() {
        conversationListLayout.loadDefaultData();
    }

    /**
     * 会话条目点击事件
     *
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {

    }
    /**
     * 会话长按菜单条目点击事件
     */
    @Override
    public boolean onMenuItemClick(MenuItem item, int position) {
        EMLog.i(TAG, "click menu position = " + position);
        return false;
    }

    /**
     * 会话长按菜单显示前的监听事件，可以对PopupMenu增加条目{@link CustomEaseConversationListLayout#addItemMenu(int, int, int, String)}，
     * 隐藏或者显示条目{@link CustomEaseConversationListLayout#findItemVisible(int, boolean)}
     *
     * @param menuHelper
     * @param position
     */
    @Override
    public void onMenuPreShow(EasePopupMenuHelper menuHelper, int position) {

    }


    @Override
    public void loadDataFinish(List<EaseConversationInfo> data) {
        finishRefresh();
    }

    @Override
    public void loadDataFail(String message) {
        finishRefresh();
    }

    /**
     * 停止刷新
     */
    public void finishRefresh() {
        if (!mContext.isFinishing() && srlRefresh != null) {
            runOnUiThread(() -> {
                srlRefresh.finishRefresh();
                srlRefresh.finishLoadMoreWithNoMoreData();
            });
        }
    }

    @Override
    public void notifyItemChange(int position) {

    }

    @Override
    public void notifyAllChange() {

    }

    @Override
    public void notifyItemRemove(int position) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        conversationListLayout.loadDefaultData();
    }
}

