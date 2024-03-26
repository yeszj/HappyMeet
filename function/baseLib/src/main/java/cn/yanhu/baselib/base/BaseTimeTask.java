package cn.yanhu.baselib.base;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import cn.yanhu.baselib.bean.TaskType;


/**
 * @author: zhengjun
 * created: 2023/12/7
 * desc:
 */
public class BaseTimeTask<Support, Result> {
    public final static String LOG_TAG = "timeTask";
    private final Handler mHandler;
    private final Runnable mRunnable;
    /**
     * 首次任务延迟执行的时间
     */
    private final long mDelay;

    private final long interval;
    /**
     * 具体的任务
     */
    private final SingleTask<Support, Result> mTask;
    /**
     * 重试次数（单次任务完成后自动重试）
     */
    private int mRepeat;


    private final OnResultListener<Result> listener;
    public long currentDelayTime;
    public final List<TaskType> taskTypeList;


    private BaseTimeTask(Builder<Support, Result> builder) {
        mDelay = builder.delay;
        taskTypeList = builder.taskTypeList;
        mTask = builder.task;
        interval = builder.interval;
        mRepeat = builder.repeat;
        listener = builder.listener;
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = () -> {
            if (mTask != null) {
                int index = 0;
                if (!isNullTaskType()) {
                    index = taskTypeList.size() - mRepeat;
                }
                if (mRepeat > 0) {
                    mRepeat--;
                }
                mTask.start(new Scheduler<Result>() {
                    @Override
                    public void process(boolean carryOn, Result result) {
                        int nextIndex;
                        if (carryOn) {
                            if (isNullTaskType()) {
                                nextIndex = -1;
                                start(interval);
                            } else {
                                nextIndex = taskTypeList.size() - mRepeat;
                                if (nextIndex < taskTypeList.size() && nextIndex >= 0) {
                                    start(taskTypeList.get(nextIndex).getKey() * 1000L);
                                }
                            }
                        } else {
                            if (isNullTaskType()) {
                                nextIndex = -1;
                            } else {
                                nextIndex = taskTypeList.size() - mRepeat;
                            }
                        }
                        if (listener != null) {
                            listener.onComplete(result, nextIndex);
                        }
                    }
                }, isNullTaskType() ? new TaskType(mRepeat, "") : taskTypeList.get(index));
            }
        };
    }

    private boolean isNullTaskType() {
        return taskTypeList == null || taskTypeList.size() <= 0;
    }

    /**
     * 开启任务
     */
    public void start(long delay) {
        if (mRepeat > 0 || mRepeat == -1) {
            if (mHandler != null && mRunnable != null) {
                currentDelayTime = delay;
                mHandler.removeCallbacks(mRunnable);
                if (delay > 0) {
                    mHandler.postDelayed(mRunnable, delay);
                } else {
                    mHandler.post(mRunnable);
                }
            }
        }
    }

    /**
     * 开启任务
     */
    public void start() {
        if (mHandler != null && mRunnable != null && (mRepeat > 0 || mRepeat == -1)) {
            currentDelayTime = mDelay;
            mHandler.removeCallbacks(mRunnable);
            if (mDelay > 0) {
                mHandler.postDelayed(mRunnable, mDelay);
            } else {
                mHandler.post(mRunnable);
            }
        }
    }

    /**
     * 取消任务
     */
    public void cancel() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            if (mTask != null) {
                mTask.cancel();
            }
        }
    }

    /**
     * 销毁任务（一般在页面关闭之后调用）
     */
    public void onDestroy() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            if (mTask != null) {
                mTask.onDestroy();
            }
        }
    }

    public static class Builder<Support, Result> {
        /**
         * 首次任务延迟执行的时间
         */
        private long delay;
        /**
         * 两次任务之间的间隔时间
         */
        private long interval;
        private List<TaskType> taskTypeList;
        /**
         * 具体的任务
         */
        private SingleTask<Support, Result> task;
        /**
         * 重试次数（单次任务完成后自动重试）
         */
        private int repeat = -1;
        private OnResultListener<Result> listener;

        public Builder<Support, Result> delay(long delay) {
            this.delay = delay;
            return this;
        }

        public Builder<Support, Result> taskList(List<TaskType> taskTypeList) {
            this.taskTypeList = taskTypeList;
            return this;
        }

        public Builder<Support, Result> interval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder<Support, Result> task(SingleTask<Support, Result> task) {
            this.task = task;
            return this;
        }

        public Builder<Support, Result> repeat(int repeat) {
            this.repeat = repeat;
            return this;
        }

        public Builder<Support, Result> callback(OnResultListener<Result> listener) {
            this.listener = listener;
            return this;
        }

        public BaseTimeTask<Support, Result> build() {
            return new BaseTimeTask<>(this);
        }

    }

    public interface SingleTask<Support, Result> {
        /**
         * 开始任务
         */
        void start(Scheduler<Result> scheduler, TaskType taskType);

        /**
         * 取消任务
         */
        void cancel();

        /**
         * 销毁任务（一般在页面退出时调用）
         */
        void onDestroy();

        /**
         * 需要从外部获取的支持参数（比如Context、goods_id）
         */
        Support getSupport();
    }

    public interface OnResultListener<Result> {
        void onComplete(Result result, int index);
    }

    public interface Scheduler<Result> {
        /**
         * 任务完成回调
         *
         * @param carryOn 是否接着重试（辅助TimingTask进行重试）
         * @param result  任务完成后回调的处理结果
         */
        void process(boolean carryOn, Result result);
    }
}
