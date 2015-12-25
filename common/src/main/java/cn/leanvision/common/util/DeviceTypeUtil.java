package cn.leanvision.common.util;

import android.content.Context;

/**
 * @author lvshicheng
 * @date 2015-1-23 11:32:37
 * @description 主要用于设备类型的操作
 */
public class DeviceTypeUtil {
    /**
     * 控制面板类型 - 通用
     */
    public static final String PANEL_TYPE_COMMON = "GENE";

    /**
     * 设备的状态
     */
    public static final String DEV_STATUS_A002 = "A002";// 插控失联（灰） 默认失联
    public static final String DEV_STATUS_A003 = "A003";// 插孔正常，设备功率小于某个值。（蓝）
    public static final String DEV_STATUS_A004 = "A004";// 正常工作（绿）
    public static final String DEV_STATUS_A005 = "A005";// 正常工作 与A002是相反状态
    /**
     * 红外类
     */
    public static final String TYPE_KGHW = "KGHW";
    /**
     * 开关类
     */
    public static final String TYPE_KG = "KG";
    /**
     * 电视机
     */
    public static final String TYPE_KGTV = "KGTV";
    /**
     * 空气净化器
     */
    public static final String TYPE_KGKJ = "KGKJ";
    /**
     * 红外热水器
     */
    public static final String TYPE_KGHE = "KGHE";
    /**
     * 红外风扇
     */
    public static final String TYPE_KGFA = "KGFA";
    /**
     * 通用红外类型
     */
    public static final String TYPE_KGGE = "KGGE";
    /**
     * 电视机顶盒
     */
    public static final String TYPE_KGST = "KGST";

    /**
     * 目前的红外设备名称
     */
    private static final String[] HW_TYPENAME = new String[]{"空调", "电视", "遥控电风扇", "电视机顶盒", "遥控音箱", "DVD播放器", "空气净化器", "玩具"};

    /**
     * 通过设备名称获取器设备大类型
     */
    public static String getBigTypeByTypeName(String typeName) {
        String bigType = TYPE_KG; // 默认开关类
        for (int i = 0; i < HW_TYPENAME.length; i++) {
            if (HW_TYPENAME[i].equals(typeName)) {
                bigType = TYPE_KGHW;
                break;
            }
        }
        return bigType;
    }

    /**
     * @param statuesType : 设备状态类型 三种类型 - 正常，用电，失联
     * @param devType     : 设备类型，如电灯...
     * @param bigType     : 设备总的分类,暂时只分为两类 - 红外(KGHW)和非红外(KG)
     * @description 更具设备类型获取相应的设备图像
     */
    public static int getDeviceIcon(Context context, String statuesType, String devType, String bigType) {
        String value = "mydevice_chazuo";
        // 为了兼容以前版本
        if (TYPE_KG.equals(bigType) || "1".equals(bigType)) {
            bigType = TYPE_KG;
        } else if (TYPE_KGHW.equals(bigType) || "2".equals(bigType)) {
            bigType = TYPE_KGHW;
        }

        // mydevice_chazuo - mydevice_chazuo_n - mydevice_chazuo_unwork - mydevice_dianbingxiang_off
        if (TYPE_KG.equals(bigType)) {
            if ("台灯".equals(devType)) {
                value = "mydevice_dengpao";
            } else if ("插座".equals(devType)) {
                value = "mydevice_chazuo";
            } else if ("手机充电器".equals(devType)) {
                value = "mydevice_chongdianqi";
            } else if ("普通电风扇".equals(devType)) {
                value = "mydevice_dianfengshan";
            } else if ("普通洗衣机".equals(devType)) {
                value = "mydevice_xiyiji";
            } else if ("热水器".equals(devType)) {
                value = "mydevice_reshuiqi";
            } else if ("饮水机".equals(devType)) {
                value = "mydevice_yinshuiji";
            } else if ("冰箱".equals(devType)) {
                value = "mydevice_dianbingxiang";
            } else {
                value = "mydevice_chazuo";
            }
        } else if (TYPE_KGHW.equals(bigType)) {
            // 红外类显示布局
            if ("空调".equals(devType)) {
                value = "mydevice_kongtiao";
            } else if ("遥控电风扇".equals(devType)) {
                value = "mydevice_yaokongfengshan";
            } else if ("遥控音箱".equals(devType)) {
                value = "mydevice_yaokongyinxiang";
            } else if ("电视机顶盒".equals(devType)) {
                value = "mydevice_dianshihezi";
            } else if ("DVD播放器".equals(devType)) {
                value = "mydevice_bofangqi";
            } else if ("空气净化器".equals(devType)) {
                value = "mydevice_kongqijinghua";
            } else if ("玩具".equals(devType)) {
                value = "mydevice_wanju";
            } else if ("电视".equals(devType)) {
                value = "mydevice_dianshiji";
            } else {
                value = "mydevice_chazuo";
            }
        } else if (TYPE_KGKJ.equals(bigType)) {
            value = "mydevice_kongqijinghua";
        } else if (TYPE_KGHE.equals(bigType)) {
            value = "mydevice_reshuiqi";
        } else if (TYPE_KGFA.equals(bigType)) {
            value = "mydevice_yaokongfengshan";
        } else if (TYPE_KGGE.equals(bigType)) {
            value = "mydevice_chazuo"; // 设置插座
        } else if (TYPE_KGTV.equals(bigType)) {
            value = "mydevice_dianshiji";
        }else if(TYPE_KGST.equals(bigType)){
            value = "mydevice_dianshihezi";
        }

        if (StringUtil.isNullOrEmpty(statuesType))
            statuesType = DEV_STATUS_A002;
        if (DEV_STATUS_A002.equals(statuesType))
            // value = value + "_unwork";
            value = value + "_off";
        else if (DEV_STATUS_A003.equals(statuesType))
            value = value + "_n";
        return context.getResources().getIdentifier(value, "drawable", context.getPackageName());
    }
}
