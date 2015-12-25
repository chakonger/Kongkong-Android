package cn.leanvision.normalkongkong.service.util;

import cn.leanvision.normalkongkong.service.LvKongCoreService;

/********************************
 * Created by lvshicheng on 15/12/22.
 * description
 ********************************/
public class LvKongCoreServiceHandler extends LvBaseServiceHandler<LvKongCoreService.CoreBinder> {

    private static LvKongCoreServiceHandler mCoreServiceUtil = new LvKongCoreServiceHandler();

    public static LvKongCoreServiceHandler getInstance() {
        return mCoreServiceUtil;
    }

    private LvKongCoreServiceHandler() {
    }

    @Override
    public Class getServiceClass() {
        return LvKongCoreService.class;
    }
}
