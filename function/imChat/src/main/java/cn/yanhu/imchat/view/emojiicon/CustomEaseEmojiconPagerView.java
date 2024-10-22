package cn.yanhu.imchat.view.emojiicon;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hyphenate.easeui.adapter.EmojiconPagerAdapter;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;

import java.util.ArrayList;
import java.util.List;

public class CustomEaseEmojiconPagerView extends ViewPager {

    private final Context context;
    private List<EaseEmojiconGroupEntity> groupEntities;

    private PagerAdapter pagerAdapter;

    private int maxPageCount;
    private int previousPagerPosition;
    private EaseEmojiconPagerViewListener pagerViewListener;
    private List<View> viewpages;

    public CustomEaseEmojiconPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomEaseEmojiconPagerView(Context context) {
        this(context, null);
    }


    public void init(List<EaseEmojiconGroupEntity> emojiconGroupList) {
        if (emojiconGroupList == null) {
            throw new RuntimeException("emojiconGroupList is null");
        }

        this.groupEntities = emojiconGroupList;

        viewpages = new ArrayList<>();
        for (int i = 0; i < groupEntities.size(); i++) {
            EaseEmojiconGroupEntity group = groupEntities.get(i);
            ChatExtendEmojiView gridViews = getEmojiView(group);
            viewpages.add(gridViews);
        }

        pagerAdapter = new EmojiconPagerAdapter(viewpages);
        setAdapter(pagerAdapter);
        setOnPageChangeListener(new EmojiPagerChangeListener());

        if (pagerViewListener != null) {
            pagerViewListener.onPagerViewInited();
        }
    }

    public void setPagerViewListener(EaseEmojiconPagerViewListener pagerViewListener) {
        this.pagerViewListener = pagerViewListener;
    }

    /**
     * set emojicon group position
     *
     * @param position
     */
    public void setGroupPostion(int position) {
            setCurrentItem(position);
    }

    /**
     * get emojicon group gridview list
     *
     * @param groupEntity
     * @return
     */
    public ChatExtendEmojiView getEmojiView(EaseEmojiconGroupEntity groupEntity) {
        List<EaseEmojicon> emojiconList = groupEntity.getEmojiconList();
        ChatExtendEmojiView chatExtendEmojiView = new ChatExtendEmojiView(context);
        chatExtendEmojiView.setEmojiData(emojiconList,groupEntity.getType());
        chatExtendEmojiView.registerOnSendEmojiListener(dataBean -> {
            if (pagerViewListener != null) {
                pagerViewListener.onExpressionClicked(dataBean);
            }
        });
        return chatExtendEmojiView;
    }


    /**
     * add emojicon group
     *
     * @param groupEntity
     */
    public void addEmojiconGroup(EaseEmojiconGroupEntity groupEntity, boolean notifyDataChange) {
        viewpages.add(getEmojiView(groupEntity));
        if (pagerAdapter != null && notifyDataChange) {
            pagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * remove emojicon group
     *
     * @param position
     */
    public void removeEmojiconGroup(int position) {
        if (position > groupEntities.size() - 1) {
            return;
        }
        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
        }
    }


    private class EmojiPagerChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            pagerViewListener.onGroupPositionChanged(position);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }


    public interface EaseEmojiconPagerViewListener {
        /**
         * pagerview initialized
         *
         */
        void onPagerViewInited();

        /**
         * group position changed
         *
         * @param groupPosition--group   position
         */
        void onGroupPositionChanged(int groupPosition);

        /**
         * page position changed
         *
         * @param oldPosition
         * @param newPosition
         */
        void onGroupInnerPagePostionChanged(int oldPosition, int newPosition);

        /**
         * group page position changed
         *
         * @param position
         */
        void onGroupPagePostionChangedTo(int position);

        /**
         * max page size changed
         *
         * @param maxCount
         */
        void onGroupMaxPageSizeChanged(int maxCount);

        void onDeleteImageClicked();

        void onExpressionClicked(EaseEmojicon emojicon);

    }

}
