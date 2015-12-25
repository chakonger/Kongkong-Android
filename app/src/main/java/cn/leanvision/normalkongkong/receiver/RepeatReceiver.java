package cn.leanvision.normalkongkong.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.leanvision.common.util.LogUtil;
import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;
import cn.leanvision.normalkongkong.service.LvKongCoreService;
import cn.leanvision.normalkongkong.service.util.LvKongCoreServiceHandler;

/********************************
 * Created by lvshicheng on 15/10/19.
 * description 定时闹钟检测后台GET状态
 ********************************/
public class RepeatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean backgroundTaskStop = SharedPrefHelper.getInstance().isBackGroundTaskStop();
        LogUtil.log(RepeatReceiver.class, "RepeatMaking " + backgroundTaskStop);
        if (backgroundTaskStop)
            return;
        LvKongCoreService.CoreBinder coreBind = LvKongCoreServiceHandler.getInstance().getCoreBind();
        if (coreBind != null)
            coreBind.initBackGetThread(false);
    }
}
