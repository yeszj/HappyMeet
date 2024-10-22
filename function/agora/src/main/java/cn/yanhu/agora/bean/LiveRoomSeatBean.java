package cn.yanhu.agora.bean;

import android.view.View;

public class LiveRoomSeatBean {

    private Integer uid;
    private View surfaceView;

    public LiveRoomSeatBean(Integer uid, View surfaceView) {
        this.uid = uid;
        this.surfaceView = surfaceView;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public View getSurfaceView() {
        return surfaceView;
    }

    public void setSurfaceView(View surfaceView) {
        this.surfaceView = surfaceView;
    }
}
