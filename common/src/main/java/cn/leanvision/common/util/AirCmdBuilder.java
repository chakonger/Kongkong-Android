package cn.leanvision.common.util;

import android.content.Context;

import cn.leanvision.common.R;

/**
 * *************************************
 *
 * @author Administrator
 * @date 2014-7-30 18:19:09空调指令生成类
 * @description *************************************
 */
public class AirCmdBuilder {

    public static void init(Context context) {
        if (null == STR_CMD_MODEL) {
            STR_CMD_OPEN_OR_CLOSE_KG = STR_CMD_OPEN_OR_CLOSE = context.getResources().getStringArray(R.array.str_cmd_open_or_close);
            STR_CMD_MODEL = context.getResources().getStringArray(R.array.str_cmd_mode);
            STR_CMD_WIND = context.getResources().getStringArray(R.array.str_cmd_wind);
            STR_CMD_WIND_OREN = context.getResources().getStringArray(R.array.str_cmd_wind_oren);
        }
    }

    // 空调模式分为五种：开关、模式、温度、风量、风向
    /**
     * 开关类的开关
     * <p/>
     * 0 - 关闭继电器 2 - 打开继电器 9 - 未知指令
     */
    public static final int INDEX_CMD_OPEN_CLOSE_KG = 0;
    public static final String[] CMD_OPEN_OR_CLOSE_KG =
            {"0", "2", "9"};
    public static String[] STR_CMD_OPEN_OR_CLOSE_KG;
//    public static String[] STR_CMD_OPEN_OR_CLOSE_KG =
//            {"关", "开"};

    /**
     * 空调类开关
     * 0 - 关 1 - 开 9 - 未知指令
     */
    public static final int INDEX_CMD_OPEN_OR_CLOSE = 1;
    public static final String[] CMD_OPEN_OR_CLOSE =
            {"0", "1", "9"};
    public static String[] STR_CMD_OPEN_OR_CLOSE;
//    public static final String[] STR_CMD_OPEN_OR_CLOSE =
//            {"关", "开"};
    /**
     * 模式
     * <p/>
     * 自动 - 0 制冷 - 1 制热 - 2 抽湿 - 3 送风 - 4 未知 - 9
     */
    public static final int INDEX_CMD_MODEL = 2;
    public static final String[] CMD_MODEL =
            {"0", "1", "2", "3", "4", "9"};
    public static String[] STR_CMD_MODEL;
    //    public static final String[] STR_CMD_MODEL =
//            {"自动", "制冷", "制热", "抽湿", "送风"};
    public static final int[] DRAWABLE_CMD_MODEL =
            {R.drawable.new_auto, R.drawable.new_cold, R.drawable.new_hot, R.drawable.new_delete_wet, R.drawable.new_wind};

    /**
     * 温度 00 - 16度 01 - 17度 ...... 10 - 32度
     * <p/>
     * 范围从16度到32度，对应的数字从00 - 10。
     * <p/>
     * 99 - 未知
     */
    public static final int INDEX_CMD_TEMP = 3;
    public static final String[] CMD_TEMP =
            {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "10"};
    public static final String[] STR_CMD_TEMP =
            {"16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32"};
    public static final String[] STR_CMD_TEMP_C =
            {"16℃", "17℃", "18℃", "19℃", "20℃", "21℃", "22℃", "23℃", "24℃", "25℃", "26℃", "27℃", "28℃", "29℃", "30℃", "31℃", "32℃"};
    /**
     * 风量
     * <p/>
     * 自动 - 0 低 - 1 中 - 2 高 - 3 未知 - 9
     */
    public static final int INDEX_CMD_WIND = 4;
    public static final String[] CMD_WIND =
            {"0", "1", "2", "3", "9"};
    public static String[] STR_CMD_WIND;
    //    public static final String[] STR_CMD_WIND =
//            {"自动", "低风", "中风", "高风"};
    public static final int[] DRAWABLE_CMD_WIND =
            {R.drawable.new_auto, R.drawable.new_smallwind, R.drawable.new_middlewind, R.drawable.new_bigwind};
    /**
     * 风向
     * <p/>
     * 自动 - 0 位置1 - 1 位置2 - 2 位置3 - 3 位置4 - 4 位置5 - 5
     */
    public static final int INDEX_CMD_WIND_OREN = 5;
    // public static final String[] CMD_WIND_OREN = { "0", "1", "3", "5", "4",
    // "5" };
    public static final String[] CMD_WIND_OREN =
            {"0", "1", "2", "3"};
    public static String[] STR_CMD_WIND_OREN;
    // public static final String[] STR_CMD_WIND_OREN = { "自动", "位置1", "位置2",
    // "位置3", "位置4", "位置5" };
//    public static final String[] STR_CMD_WIND_OREN =
//            {"自动", "位置1", "位置2", "位置3"};
    public static final int[] DRAWABLE_CMD_WIND_OREN =
            {R.drawable.new_auto, R.drawable.new_wind_low, R.drawable.new_wind_middle, R.drawable.new_wind_high};
    // public static final String[] STR_CMD_WIND_OREN = { "自动", "竖吹", "斜吹", "平吹"
    // };

    /**
     * 获取空调默认指令
     */
    public static String getDefaultKGHW() {
        return CMD_OPEN_OR_CLOSE[0] + CMD_MODEL[0] + CMD_TEMP[8] + CMD_WIND[0] + CMD_WIND_OREN[0];
    }

    /**
     * @param typeNum 指定指令类型集合
     * @param strCode 指定指令名称
     */
    public static String getCmdCode(int typeNum, String strCode) {
        String result = "";
        int index = 0;
        switch (typeNum) {
            case INDEX_CMD_OPEN_OR_CLOSE:
            case INDEX_CMD_MODEL:
                index = getResult(STR_CMD_MODEL, strCode);
                result = CMD_MODEL[index];
                break;
            case INDEX_CMD_TEMP:
                index = getResult(STR_CMD_TEMP, strCode);
                result = CMD_TEMP[index];
                break;
            case INDEX_CMD_WIND:
                index = getResult(STR_CMD_WIND, strCode);
                result = CMD_WIND[index];
                break;
            case INDEX_CMD_WIND_OREN:
                index = getResult(STR_CMD_WIND_OREN, strCode);
                result = CMD_WIND_OREN[index];
                break;
            default:
                break;
        }
        // LogUtil.log("strCode : " + strCode + " -- result : " + result);
        return result;
    }

    /**
     * @param typeNum 指定指令类型集合
     * @param cmdCode 指令指令code
     */
    public static String getStrCode(int typeNum, String cmdCode) {
        // 如果是"F"或者"FF"的指令不予以处理
        if (StringUtil.isNullOrEmpty(cmdCode.replace("F", ""))) {
            return "";
        }
        String result = "";
        int index;
        switch (typeNum) {
            case INDEX_CMD_OPEN_OR_CLOSE:
                index = getResult(CMD_OPEN_OR_CLOSE, cmdCode);
                result = STR_CMD_OPEN_OR_CLOSE[index];
                break;
            case INDEX_CMD_MODEL:
                index = getResult(CMD_MODEL, cmdCode);
                result = STR_CMD_MODEL[index];
                break;
            case INDEX_CMD_TEMP:
                index = getResult(CMD_TEMP, cmdCode);
                result = STR_CMD_TEMP[index];
                break;
            case INDEX_CMD_WIND:
                index = getResult(CMD_WIND, cmdCode);
                result = STR_CMD_WIND[index];
                break;
            case INDEX_CMD_WIND_OREN:
                index = getResult(CMD_WIND_OREN, cmdCode);
                result = STR_CMD_WIND_OREN[index];
                break;
            default:
                break;
        }
        return result;
    }

    private static int getResult(String[] strList, String strCode) {
        int index = 0;
        for (int i = 0; i < strList.length; i++) {
            if (strList[i].equals(strCode)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
