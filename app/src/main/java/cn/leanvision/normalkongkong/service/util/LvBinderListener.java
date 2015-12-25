package cn.leanvision.normalkongkong.service.util;

import android.os.IBinder;

/**
 * 服务绑定监听类
 */
public abstract class LvBinderListener {

    protected String param;

    protected int eventType;

    public LvBinderListener() {
    }

    public LvBinderListener(String param) {
        this.param = param;
    }

    public void setEvent(int eventType) {
        this.eventType = eventType;
    }

    public abstract void bindSucceed(IBinder service, String param);
}
