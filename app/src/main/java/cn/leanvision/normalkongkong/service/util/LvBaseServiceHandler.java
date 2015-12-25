package cn.leanvision.normalkongkong.service.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import cn.leanvision.common.util.LogUtil;
import cn.leanvision.normalkongkong.LvApplication;

/**
 * @author lvshicheng
 * @date 2015-07-13 11:47:06
 * @description service工具基类
 */
public abstract class LvBaseServiceHandler<T extends Binder> {
    private T mBinder;

    private List<LvBinderListener> mBinderListeners;

    private ServiceConnection coreServiceConnect;

    public T getCoreBind() {
        bindCoreBinder();
        LogUtil.log(getClass(), null == mBinder ? "Binder is Null" : "Binder is not Null");
        return mBinder;
    }

    protected LvBaseServiceHandler() {
        mBinderListeners = new ArrayList<>();
    }

    public void bindCoreBinder() {
        final LvApplication context = LvApplication.getInstance();
        if (mBinder == null) {
            coreServiceConnect = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    LogUtil.log("Core服务绑定成功");
                    mBinder = (T) service;
                    dispatchSucceed(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mBinder = null;
                    coreServiceConnect = null;
                }
            };
            Intent intent = new Intent(context, getServiceClass());
            context.bindService(intent, coreServiceConnect, Activity.BIND_AUTO_CREATE);
        } else {
            dispatchSucceed(mBinder);
        }
    }

    protected void dispatchSucceed(IBinder service) {
        if (!mBinderListeners.isEmpty()) {
            for (int i = 0; i < mBinderListeners.size(); i++) {
                LvBinderListener binderListener = mBinderListeners.get(i);
                binderListener.bindSucceed(service, binderListener.param);
            }
        }
    }

    public void removeBind() {
        final LvApplication context = LvApplication.getInstance();
        if (coreServiceConnect != null) {
            context.unbindService(coreServiceConnect);
            mBinder = null;
            coreServiceConnect = null;
        }
    }

    /**
     * 注册服务绑定监听类
     */
    public void registerCallBack(LvBinderListener mBinderListener) {
        if (mBinderListeners.contains(mBinderListener))
            mBinderListeners.remove(mBinderListener);
        mBinderListeners.add(mBinderListener);
    }

    public void unregisterCallBack(LvBinderListener mBinderListener) {
        mBinderListeners.remove(mBinderListener);
    }

    public abstract Class getServiceClass();
}
