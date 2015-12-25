package cn.leanvision.normalkongkong.framework;

import android.app.Activity;
import android.os.Handler;

import java.lang.ref.WeakReference;

/********************************
 * Created by lvshicheng on 15/10/18.
 * description
 ********************************/
public abstract class LvIBaseHandler<T extends Activity> extends Handler{

    private final WeakReference<T> weakT;

    public LvIBaseHandler(T t){
        weakT = new WeakReference<T>(t);
    }

    protected T getActivity(){
        if(weakT == null)
            return null;
        return weakT.get();
    }

    protected boolean canGoNext(){
        if (getActivity() == null)
            return false;
        else
            return true;
    }
}
