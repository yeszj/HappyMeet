package cn.yanhu.imchat.view.emojiicon;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hyphenate.easeui.R;
import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.GlideUtils;

import java.util.ArrayList;
import java.util.List;


public class CustomEaseEmojiconScrollTabBar extends RelativeLayout {

    private Context context;
    private HorizontalScrollView scrollView;
    private LinearLayout tabContainer;

    private final List<View> tabList = new ArrayList<>();
    private EaseScrollTabBarItemClickListener itemClickListener;

    public CustomEaseEmojiconScrollTabBar(Context context) {
        this(context, null);
    }

    public CustomEaseEmojiconScrollTabBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public CustomEaseEmojiconScrollTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_emojicon_tab_bar, this);

        scrollView = findViewById(R.id.scroll_view);
        tabContainer = findViewById(R.id.tab_container);
    }

    /**
     * add tab
     */
    public void addTab(String icon) {
        View tabView = View.inflate(context,R.layout.ease_scroll_tab_item, null);
        ImageView imageView = tabView.findViewById(R.id.iv_icon);
        if (icon.endsWith(".svg")){
            loadTabSvg(icon, imageView);
        }else {
            loadTabImage(icon, imageView);
        }
        tabView.setLayoutParams(new ViewGroup.LayoutParams(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_62),CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_44)));
        tabContainer.addView(tabView);
        tabList.add(tabView);
        final int position = tabList.size() - 1;
        tabView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
    }

    private void loadTabSvg(String icon, ImageView imageView) {
        if (context instanceof Activity && ((Activity) context).isDestroyed()){
            return;
        }
        Glide.with(context).as(
                        PictureDrawable.class
                )
                .load(icon).into(new CustomTarget<PictureDrawable>() {
                    @Override
                    public void onResourceReady(@NonNull PictureDrawable resource, @Nullable Transition<? super PictureDrawable> transition) {
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void loadTabImage(String icon, ImageView imageView) {
        GlideUtils.INSTANCE.loadAsDrawable(context, icon, new CustomTarget<Drawable>() {

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition transition) {
                imageView.setImageDrawable(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    /**
     * remove tab
     *
     * @param position
     */
    public void removeTab(int position) {
        tabContainer.removeViewAt(position);
        tabList.remove(position);
    }

    public void selectedTo(int position) {
        scrollTo(position);
        for (int i = 0; i < tabList.size(); i++) {
            if (position == i) {
                tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emojicon_tab_selected));
            } else {
                tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emojicon_tab_nomal));
            }
        }
    }

    private void scrollTo(final int position) {
        int childCount = tabContainer.getChildCount();
        if (position < childCount) {
            scrollView.post(() -> {
                int mScrollX = tabContainer.getScrollX();
                int childX = (int) ViewCompat.getX(tabContainer.getChildAt(position));

                if (childX < mScrollX) {
                    scrollView.scrollTo(childX, 0);
                    return;
                }

                int childWidth = tabContainer.getChildAt(position).getWidth();
                int hsvWidth = scrollView.getWidth();
                int childRight = childX + childWidth;
                int scrollRight = mScrollX + hsvWidth;

                if (childRight > scrollRight) {
                    scrollView.scrollTo(childRight - scrollRight, 0);
                }
            });
        }
    }


    public void setTabBarItemClickListener(EaseScrollTabBarItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public interface EaseScrollTabBarItemClickListener {
        void onItemClick(int position);
    }

}
