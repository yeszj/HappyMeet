package cn.yanhu.imchat.custom.chat;

import com.hyphenate.easeui.adapter.EaseBaseDelegateAdapter;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;

import cn.yanhu.imchat.R;


public class CustomEaseConversationListAdapter extends EaseBaseDelegateAdapter<EaseConversationInfo> {
    private int emptyLayoutId;

    private boolean isIntimacy;


    @Override
    public int getEmptyLayoutId() {
        return emptyLayoutId != 0 ? emptyLayoutId : (!isIntimacy ? R.layout.ease_layout_default_no_conversation_data : R.layout.ease_layout_intimacy_no_conversation_data);
    }

    /*
     * false：全部，true：亲密
     * */
    public void setListType(boolean isIntimacy) {
        this.isIntimacy = isIntimacy;
    }

    /**
     * set empty layout
     *
     * @param layoutId
     */
    public void setEmptyLayoutId(int layoutId) {
        this.emptyLayoutId = layoutId;
        notifyDataSetChanged();
    }

}

