package cn.yanhu.commonres.view.floatScreenView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.yanhu.commonres.bean.BaseUserInfo;
import cn.yanhu.commonres.bean.ChatRoomGiftMsg;
import cn.yanhu.commonres.bean.GiftIdentify;
import cn.yanhu.commonres.bean.GiftInfo;
import cn.yanhu.commonres.R;

/**
 * @author: witness
 * created: 2022/8/4
 * desc:
 */
public class RewardLayout extends LinearLayout {

    public final String TAG = this.getClass().getSimpleName();

    public static final int MAX_COUNT_DEFAULT = 3;
    public static final int MAX_THREAD = 1;
    public static final int MIN_CLEAR_TIME = 50;
    private int MIN_TAKE_TIME = 200;
    private int MAX_GIFT_COUNT;
    private int GIFT_ITEM_LAYOUT;
    private List<ChatRoomGiftMsg> beans;
    private RewardAnimAdapter adapter;
    private AnimationSet outAnim = null;
    private ScheduledExecutorService clearService;
    private ScheduledExecutorService takeService;
    private GiftClearer clearer;
    private GiftTaker taker;
    private GiftBasket basket;
    private GiftInterface clearTask;
    private GiftInterface takeTask;

    public interface RewardAnimAdapter<T extends GiftIdentify> {
        /**
         * 初始化
         *
         * @param view
         * @param bean
         * @return
         */
        View onInit(View view, T bean);

        /**
         * 更新
         *
         * @param view
         * @param o    原礼物对象
         * @param t    新礼物对象
         * @return
         */
        View onUpdate(View view, T o, T t);

        /**
         * 礼物展示结束，可能由于送礼者过多，轨道被替换导致结束
         *
         * @param bean
         * @return
         */
        void onKickEnd(T bean);

        /**
         * 礼物连击结束,即被系统自动清理时回调
         *
         * @param bean
         * @return
         */
        void onComboEnd(T bean);

        /**
         * 添加进入动画
         *
         * @param view
         */
        void addAnim(View view);

        /**
         * 添加退出动画
         *
         * @return
         */
        AnimationSet outAnim();

        /**
         * 鉴别礼物唯一性，
         *
         * @param o 已存在的礼物bean
         * @param t 新传入的礼物bean
         * @return 返回比对后的结果
         */
        boolean checkUnique(T o, T t);

        T generateBean(T bean);

        void onClear();
    }

    public RewardAnimAdapter getAdapter() {
        return adapter;
    }

    public void setRewardAnimAdapter(RewardAnimAdapter adapter) {
        this.adapter = adapter;
    }

    public RewardLayout(Context context) {
        super(context);
        init();
    }

    public RewardLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RewardLayout);
        MAX_GIFT_COUNT = a.getInteger(R.styleable.RewardLayout_max_gift, MAX_COUNT_DEFAULT);
        GIFT_ITEM_LAYOUT = a.getResourceId(R.styleable.RewardLayout_gift_item_layout, 0);
        init();
        a.recycle();
    }

    public RewardLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RewardLayout);
        MAX_GIFT_COUNT = (int) a.getDimension(R.styleable.RewardLayout_max_gift, MAX_COUNT_DEFAULT);
        GIFT_ITEM_LAYOUT = a.getResourceId(R.styleable.RewardLayout_gift_item_layout, 0);
        init();
        a.recycle();
    }

    /**
     * 测量礼物view的高度和宽度
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View child = getGiftView();
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams lp = (MarginLayoutParams) child
                .getLayoutParams();
        // 当前子空间实际占据的宽度,此处已用FrameLayout包裹，这里的margin≡0，
        int childWidth = child.getMeasuredWidth() + lp.leftMargin
                + lp.rightMargin;
        // 当前子空间实际占据的高度
        int childHeight = child.getMeasuredHeight() + lp.topMargin
                + lp.bottomMargin;

        int totalWidth = childWidth + getPaddingLeft() + getPaddingRight();
        int totalHeight = childHeight * MAX_GIFT_COUNT + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(measureWidth(widthMeasureSpec, totalWidth, child.getLayoutParams().width)
                , measureHeight(heightMeasureSpec, totalHeight, child.getLayoutParams().height));
    }

    /**
     * 测量宽度，结合item布局的宽参数
     *
     * @param measureSpec
     * @param viewGroupWidth
     * @param childLp
     * @return
     */
    private int measureWidth(int measureSpec, int viewGroupWidth, int childLp) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                if (childLp == ViewGroup.LayoutParams.MATCH_PARENT) {
                    result = specSize;
                } else {
                    result = viewGroupWidth + specSize;//Math.min(viewGroupWidth, specSize)
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                result = specSize;
                break;
        }
        return result;
    }

    /**
     * 测量高度，结合item布局的高参数
     *
     * @param measureSpec
     * @param viewGroupHeight
     * @param childLp
     * @return
     */
    private int measureHeight(int measureSpec, int viewGroupHeight, int childLp) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                if (childLp == ViewGroup.LayoutParams.MATCH_PARENT) {
                    result = specSize;
                } else {
                    result = Math.min(viewGroupHeight, specSize);
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                result = specSize;
                break;
        }
        return result;
    }

    private void init() {
        beans = new ArrayList<>();
        clearTask = () -> {
            try {
                clearTask();
            } catch (Exception e) {
                Log.d(TAG, "clearException=" + e.getMessage());
            }
        };
        takeTask = this::takeTask;
        clearer = new GiftClearer(clearTask);
        basket = new GiftBasket();
        taker = new GiftTaker(takeTask);
        clearService = Executors.newScheduledThreadPool(MAX_THREAD);
        takeService = Executors.newScheduledThreadPool(MAX_THREAD);
        startClearService();
        startTakeGiftService();
    }

    public int getMIN_TAKE_TIME() {
        return MIN_TAKE_TIME;
    }

    /**
     * 设置最小展示礼物时间（不要设置过小的值，太小的时间UI显示不清楚也没有意义）
     *
     * @param MIN_TAKE_TIME
     */
    public void setMIN_TAKE_TIME(int MIN_TAKE_TIME) {
        this.MIN_TAKE_TIME = MIN_TAKE_TIME;
    }


    /**
     * 向rewardlayout中添加MAX_GIFT_COUNT个子framelayout
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getChildCount() != 0) {
            removeAllViews();
        }
        for (int i = 0; i < MAX_GIFT_COUNT; i++) {
            FrameLayout linearLayout = new FrameLayout(getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                    (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / MAX_GIFT_COUNT);
            linearLayout.setLayoutParams(params);
            addView(linearLayout);
        }
    }

    /**
     * 外部调用方法，添加礼物view到rewardlayout中
     *
     * @param sBean
     */
    private void showGift(ChatRoomGiftMsg sBean) {
        if (adapter == null) {
            return;
        }
        ChatRoomGiftMsg bean = null;
        for (ChatRoomGiftMsg baseGiftBean : beans) {
            if (adapter.checkUnique(baseGiftBean, sBean)) {
                bean = baseGiftBean;
            }
        }
        if (bean == null) {
            bean = (ChatRoomGiftMsg) adapter.generateBean(sBean);
            if (bean == null) {
                throw new NullPointerException("clone return null");
            }
            beans.add(bean);
        }
        ChatRoomGiftMsg mBean;
        View giftView = findSameUserGiftView(bean);
        /*该用户不在礼物显示列表*/
        if (giftView == null) {
            mBean = bean;
            /*如果正在显示的礼物的个数超过MAX_GIFT_COUNT个，那么就移除最后一次更新时间比较长的*/
            if (getCurrentGiftCount() > MAX_GIFT_COUNT - 1) {
                List<GiftIdentify> list = new ArrayList();
                for (int i = 0; i < getChildCount(); i++) {
                    ViewGroup vg = (ViewGroup) getChildAt(i);
                    for (int j = 0; j < vg.getChildCount(); j++) {
                        if (vg.getChildAt(j).isEnabled()) {
                            GiftIdentify gBean = (GiftIdentify) vg.getChildAt(j).getTag();
                            gBean.setTheCurrentIndex(i);
                            list.add(gBean);
                        }
                    }
                }
                // 根据加入时间排序所有child中giftview
                Collections.sort(list);
                if (list.size() > 0) {
                    removeGiftViewAnim(findSameUserGiftView(list.get(0)));
                }
                addGiftViewAnim(mBean);
            } else {
                addGiftViewAnim(mBean);
            }

        } else {
            if (giftView.isEnabled()) {
                mBean = (ChatRoomGiftMsg) giftView.getTag();
                // 相同礼物需要更新SendGiftSize
                GiftInfo giftInfo = sBean.getGiftInfo();
                GiftInfo mGiftInfo = mBean.getGiftInfo();
                mGiftInfo.setSendNumber(giftInfo.getSendNumber());
                if (adapter != null) {
                    giftView = adapter.onUpdate(giftView, mBean, sBean);
                }
                mBean.setTheLatestRefreshTime(System.currentTimeMillis());
                giftView.setTag(mBean);
                ViewGroup vg = (ViewGroup) giftView.getParent();
                vg.setTag(mBean.getTheLatestRefreshTime());
            }
        }
    }

    /**
     * 手动更新礼物过期时间
     *
     * @param cBean
     */
    public void updateRefreshTime(GiftIdentify cBean) {
        updateRefreshTime(cBean, 0);
    }

    /**
     * 手动更新礼物过期时间
     *
     * @param cBean
     * @param delay
     */
    public void updateRefreshTime(GiftIdentify cBean, long delay) {
        if (adapter == null) {
            throw new IllegalArgumentException("setAdapter first");
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final int index = i;
            ViewGroup viewG = (ViewGroup) getChildAt(index);
            for (int j = 0; j < viewG.getChildCount(); j++) {
                final View view = viewG.getChildAt(j);
                final GiftIdentify tag = (GiftIdentify) view.getTag();
                if (tag != null && view.isEnabled()) {
                    if (adapter.checkUnique(tag, cBean)) {
                        if (delay == 0) {
                            if (cBean.getTheLatestRefreshTime() != 0 && cBean.getTheLatestRefreshTime() > tag.getTheLatestRefreshTime()) {
                                tag.setTheLatestRefreshTime(cBean.getTheLatestRefreshTime());
                            } else {
                                tag.setTheLatestRefreshTime(System.currentTimeMillis());
                            }
                        } else {
                            tag.setTheLatestRefreshTime(tag.getTheLatestRefreshTime() + delay);
                        }
                    }
                }
            }

        }
    }

    /**
     * 从xml布局中加载礼物view
     */
    private View getGiftView() {
        FrameLayout root = new FrameLayout(getContext());
        View view = LayoutInflater.from(getContext()).inflate(getGiftRes(), root, false);
        LayoutParams lp = new LayoutParams(view.getLayoutParams().width, view.getLayoutParams().height);
        root.setLayoutParams(lp);
        root.addView(view);
        return root;
    }


    private void addGiftViewAnim(final GiftIdentify mBean) {
        View giftView = null;

        if (adapter != null) {
            giftView = adapter.onInit(getGiftView(), mBean);
        }

        mBean.setTheLatestRefreshTime(System.currentTimeMillis());

        giftView.setTag(mBean);
        // 标记该giftview可用
        giftView.setEnabled(true);

        addChildGift(giftView);
        invalidate();

        if (adapter != null) {
            adapter.addAnim(giftView);
        }

    }

    /**
     * 删除指定framelayout下的view的礼物动画
     *
     * @param view
     */
    private void removeGiftViewAnim(final View view) {
        if (view != null) {
            // 标记该giftview不可用
            view.setEnabled(false);
            if (adapter != null) {
                adapter.onKickEnd((GiftIdentify) view.getTag());
                outAnim = adapter.outAnim();
                outAnim.setFillAfter(true);
                outAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                removeChildGift(view);
                            }
                        });

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                post(new Runnable() {
                    @Override
                    public void run() {
                        view.startAnimation(outAnim);
                    }
                });
            }

        }
    }

    /**
     * 删除指定framelayout下的view的礼物动画
     *
     * @param index
     */
    private void removeGiftViewAnim(final int index) {
        final View removeView = getChildGift(index);
        if (removeView != null) {
            // 标记该giftview不可用
            removeView.setEnabled(false);
            if (adapter != null) {
                adapter.onComboEnd((GiftIdentify) removeView.getTag());
                outAnim = adapter.outAnim();
                outAnim.setFillAfter(true);
                outAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        post(() -> removeChildGift(removeView));

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                post(() -> removeView.startAnimation(outAnim));

            }

        }
    }


    /**
     * 定时清除礼物
     */
    private void startClearService() {
        if (clearService.isShutdown()) {
            clearService = Executors.newScheduledThreadPool(MAX_THREAD);
        }
        clearService.scheduleWithFixedDelay(clearer, 0, MIN_CLEAR_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 不断取礼物队列
     */
    private void startTakeGiftService() {
        if (MIN_TAKE_TIME < MIN_CLEAR_TIME) {
            throw new IllegalArgumentException("Illegal MIN_TAKE_TIME");
        }
        if (takeService.isShutdown()) {
            takeService = Executors.newScheduledThreadPool(MAX_THREAD);
        }
        takeService.scheduleWithFixedDelay(taker, 0, MIN_TAKE_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 移除指定framelayout下面的礼物view
     *
     * @param index
     */
    private void removeChildGift(int index) {
        if (index < getChildCount() && getChildAt(index) instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) getChildAt(index);
            if (vg.getChildCount() > 0) {
                vg.removeViewAt(0);
            }
        }
    }

    /**
     * 移除指定framelayout下面的礼物view
     *
     * @param view
     */
    private void removeChildGift(View view) {
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup vg = (ViewGroup) getChildAt(i);
            if (vg==null){
                break;
            }
            final int index = vg.indexOfChild(view);
            if (index >= 0) {
                ChatRoomGiftMsg bean = (ChatRoomGiftMsg) view.getTag();
                GiftInfo giftInfo = bean.getGiftInfo();
                int giftId = giftInfo.getId();
                BaseUserInfo sendUserInfo = bean.getSendUser();
                String userId = sendUserInfo.getUserId();
                for (Iterator<ChatRoomGiftMsg> it = beans.iterator(); it.hasNext(); ) {
                    ChatRoomGiftMsg value = it.next();
                    BaseUserInfo sendUser = value.getSendUser();
                    if (sendUser==null){
                        return;
                    }
                    if (value.getGiftInfo().getId()==giftId && sendUser.getUserId().equals(userId)) {
                        it.remove();
                    }
                }
                vg.removeView(view);
                view = null;
                onClear();
            }
        }
    }

    public void onClear() {
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup vg = (ViewGroup) getChildAt(i);
            if (vg==null){
                break;
            }
            int childCount = vg.getChildCount();
            if (childCount > 0) {
                return;
            }
        }
        if (adapter!=null){
            adapter.onClear();
        }
    }

    public void clear(){
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup vg = (ViewGroup) getChildAt(i);
            if (vg==null){
                break;
            }
            vg.removeAllViews();
        }
    }

    /**
     * 获取当前在显示的礼物数量
     *
     * @return
     */
    public int getCurrentGiftCount() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup itemGroup = (ViewGroup) getChildAt(i);
            for (int j = 0; j < itemGroup.getChildCount(); j++) {
                if (itemGroup.getChildAt(j).isEnabled()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getCurrentViewCount() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup itemGroup = (ViewGroup) getChildAt(i);
            if (itemGroup.getChildCount() > 0) {
                count++;
            }
        }

        return count;
    }

    /**
     * 获取指定framelayout下的礼物view
     *
     * @param index
     * @return
     */
    private View getChildGift(int index) {
        View view = null;
        ViewGroup vg = (ViewGroup) getChildAt(index);
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i).isEnabled()) {
                view = vg.getChildAt(i);
            }
        }
        return view;
    }

    /**
     * 添加礼物到空闲的framelayout，在覆盖的时候可能存在礼物离场动画还么结束view还没有被remove的情况下
     * 根据该view的enable判断
     *
     * @param view
     */
    private void addChildGift(View view) {
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup itemGroup = (ViewGroup) getChildAt(i);
            if (itemGroup.getChildCount() == 0) {
                itemGroup.addView(view);
                itemGroup.setTag(((GiftIdentify) view.getTag()).getTheLatestRefreshTime());
                break;
            } else {
                boolean isAllCancel = true;
                for (int j = 0; j < itemGroup.getChildCount(); j++) {
                    if (itemGroup.getChildAt(j).isEnabled()) {
                        isAllCancel = false;
                        break;
                    }
                }
                if (isAllCancel) {
                    itemGroup.addView(view);
                    itemGroup.setTag(((GiftIdentify) view.getTag()).getTheLatestRefreshTime());
                    break;
                }
            }
        }
    }

    /**
     * 找出唯一识别的礼物
     *
     * @param target
     * @return
     */
    private View findSameUserGiftView(GiftIdentify target) {
        if (adapter == null) {
            return null;
        }
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup itemGroup = (ViewGroup) getChildAt(i);
            for (int j = 0; j < itemGroup.getChildCount(); j++) {
                GiftIdentify rGiftBean = (GiftIdentify) itemGroup.getChildAt(j).getTag();
                if (adapter.checkUnique(rGiftBean, target) && itemGroup.getChildAt(j).isEnabled()) {
                    return itemGroup.getChildAt(j);
                }
            }
        }
        return null;
    }

    public void onPause() {
        if (clearService != null) {
            clearService.shutdown();
        }
        if (takeService != null) {
            takeService.shutdown();
        }
    }

    public void onResume() {
        if (clearService != null) {
            if (clearService.isShutdown()) {
                startClearService();
            }
        } else {
            clearService = Executors.newScheduledThreadPool(MAX_THREAD);
            startClearService();
        }
        if (takeService != null) {
            if (takeService.isShutdown()) {
                startTakeGiftService();
            }
        } else {
            takeService = Executors.newScheduledThreadPool(MAX_THREAD);
            startTakeGiftService();
        }
    }

    public void onDestroy() {
        if (clearService != null) {
            clearService.shutdownNow();
            clearService = null;
        }
        if (takeService != null) {
            takeService.shutdownNow();
            takeService = null;
        }
        clearTask = null;
        takeTask = null;
        clearer = null;
        taker = null;
        basket = null;
        adapter = null;
    }


    private int getGiftRes() {
        if (GIFT_ITEM_LAYOUT != 0) {
            return GIFT_ITEM_LAYOUT;
        } else {
            throw new NullPointerException("u should init gift item resource first");
        }
    }

    public void setMaxGift(int max) {
        MAX_GIFT_COUNT = max;
    }

    public int getMaxGiftCount() {
        return MAX_GIFT_COUNT;
    }

    /**
     * before view attachtowindow
     *
     * @param res
     */
    public void setGiftItemRes(int res) {
        GIFT_ITEM_LAYOUT = res;
    }

    /**
     * 将礼物放入队列
     *
     * @param bean
     */
    public void put(GiftIdentify bean) {
        if (basket != null) {
            try {
                basket.putGift(bean);
            } catch (InterruptedException e) {
                Log.d(TAG, "IllegalStateException=" + e.getMessage());
            }
        }
    }


    public interface GiftInterface {
        void doSomething();
    }

    /**
     * 清礼物并执行礼物连击结束回调
     */
    private void clearTask() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final int index = i;
            ViewGroup viewG = (ViewGroup) getChildAt(index);
            int childCount = viewG.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = viewG.getChildAt(j);
                // 可能判断完获取的时候还是为空,保底try catch，如果改用handler postDelay去倒计时，频繁的取消，
                // 开始,维护每个倒计时，修改时间,也会很麻烦，暂时先这么处理，有什么改进方案，欢迎pr o_o!!!
                if (view != null) {
                    final GiftIdentify tag = (GiftIdentify) view.getTag();
                    if (tag != null && view.isEnabled()) {
                        long nowtime = System.currentTimeMillis();
                        long upTime = tag.getTheLatestRefreshTime();
                        if ((nowtime - upTime) >= tag.getTheGiftStay()) {
                            post(() -> removeGiftViewAnim(index));
                        }
                    }
                }
            }

        }
    }

    /**
     * 取礼物
     */
    private void takeTask() {
        boolean interrupted = false;
        try {
            final ChatRoomGiftMsg gift = (ChatRoomGiftMsg) basket.takeGift();
            if (gift != null) {
                post(() -> showGift(gift));
            }
        } catch (InterruptedException e) {
            interrupted = true;
            Log.d(TAG, "InterruptedException=" + e.getMessage());
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException=" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.d(TAG, "Exception=" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 礼物清理者
     */
    public static class GiftClearer implements Runnable {

        private final GiftInterface mInterface;

        public GiftClearer(GiftInterface mInterface) {
            this.mInterface = mInterface;
        }

        @Override
        public void run() {
            if (mInterface != null) {
                mInterface.doSomething();
            }
        }
    }

    /**
     * 礼物消费者
     */
    public static class GiftTaker implements Runnable {


        private final GiftInterface mInterface;

        public GiftTaker(GiftInterface mInterface) {
            this.mInterface = mInterface;
        }

        @Override
        public void run() {
            if (mInterface != null) {
                mInterface.doSomething();
            }
        }

    }

    /**
     * 礼物仓库队列
     */
    public static class GiftBasket {

        private final String TAG = "GiftBasket";

        BlockingQueue<GiftIdentify> queue;

        public GiftBasket() {
            queue = new LinkedBlockingQueue<>(10);
        }

        /**
         * 将礼物放入队列
         *
         * @param bean
         */
        public void putGift(GiftIdentify bean) throws InterruptedException {
            //添加元素到队列，如果队列已满,线程进入等待，直到有空间继续生产
            queue.put(bean);
            Log.d(TAG, "puted size:" + queue.size());
        }

        /**
         * 从队列取出礼物
         *
         * @return
         */
        public GiftIdentify takeGift() throws InterruptedException {
            //检索并移除队列头部元素，如果队列为空,线程进入等待，直到有新的数据加入继续消费
            GiftIdentify bean = queue.take();
            Log.d(TAG, "taked size:" + queue.size());
            //检索并删除队列头部元素，如果队列为空，抛出异常，退出消费模式
//        GiftIdentify bean = queue.remove();
            //检索并删除队列头部元素，如果队列为空，返回false，否则返回true，继续消费
//        GiftIdentify bean = queue.poll();
            //检索并删除队列头部元素，如果队列为空，则等待指定时间，成功返回true，否则返回false，继续消费
//        GiftIdentify bean = queue.poll(3, TimeUnit.SECONDS);
            return bean;
        }
    }
}
