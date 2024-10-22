package cn.yanhu.commonres.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Choreographer;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *@date on 2019/3/25
 *@describe 监听跑马灯状态，是否完成
 */
public class MyMarqueeTextView extends AppCompatTextView {


    private String TAG = MyMarqueeTextView.class.getSimpleName();

    private static final byte MARQUEE_STOPPED = 0x0;
    private static final byte MARQUEE_STARTING = 0x1;
    private static final byte MARQUEE_RUNNING = 0x2;

    private OnMarqueeListener onMarqueeListener;

    public MyMarqueeTextView(Context context) {
        super(context);

    }

    public MyMarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyMarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {

    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    @Override
    public boolean post(Runnable action) {
        return super.post(action);
    }

    public void setMarqueeListener(OnMarqueeListener onMarqueeListener) {
        this.onMarqueeListener = onMarqueeListener;
        try {
            Field marquee = this.getClass().getSuperclass().getDeclaredField("mMarquee");
            if(marquee != null) {
                marquee.setAccessible(true);
                Object obj = marquee.get(this);
                if(obj != null) {
                    Class cls = obj.getClass();
                    Field field = cls.getDeclaredField("mStatus");
                    if(field != null) {
                        field.setAccessible(true);
                    }
                    Field field1 = cls.getDeclaredField("mRestartCallback");
                    if(field1 != null) {
                        field1.setAccessible(true);
                        field1.set(obj, mRestartCallback);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Choreographer.FrameCallback mRestartCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {

            try {
                Field marquee = MyMarqueeTextView.this.getClass().getSuperclass().getDeclaredField("mMarquee");
                if(marquee != null) {
                    marquee.setAccessible(true);
                    Object obj = marquee.get(MyMarqueeTextView.this);
                    if(obj != null) {
                        Class cls = obj.getClass();
                        Field field = cls.getDeclaredField("mStatus");
                        if(field != null) {
                            field.setAccessible(true);
                            byte mStatus = ((Byte)field.get(obj)).byteValue();
                            if(mStatus == MARQUEE_RUNNING) {
                                Field field1 = cls.getDeclaredField("mRepeatLimit");
                                field1.setAccessible(true);
                                int mRepeatLimit = ((Integer)field1.get(obj)).intValue();;
                                if(mRepeatLimit >= 0) {
                                    mRepeatLimit--;
                                }
                                if(onMarqueeListener != null) {
                                    onMarqueeListener.onMarqueeRepeateChanged(mRepeatLimit);
                                }
                                Method method = cls.getDeclaredMethod("start", Integer.TYPE);
                                method.setAccessible(true);
                                method.invoke(obj, mRepeatLimit);
                            }
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public boolean isFocused() {
        return true;
    }

    //状态监听接口
    public interface OnMarqueeListener {
        void onMarqueeRepeateChanged(int repeatLimit);
    }

}
