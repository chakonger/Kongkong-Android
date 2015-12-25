package cn.leanvision.normalkongkong;

import android.app.Application;

import cn.leanvision.common.util.AirCmdBuilder;

/********************************
 * Created by lvshicheng on 15/12/21.
 * description
 ********************************/
public class LvApplication extends Application {

    private static LvApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        AirCmdBuilder.init(this);
    }

    public static LvApplication getInstance() {
        return instance;
    }
}
