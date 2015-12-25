package cn.leanvision.common.util;

/*****************************
 * @author SHICHENG LV
 * @date 2015-5-11 下午3:03:46
 * @description : 记录当前可识别action
 *****************************/
public class CmdAction {
    /**
     * 空调默认的指令
     */
    public static final String DEFAULT_INST = "000800";

    public static final String CMD_ACTION_CLOSE = "0";
    public static final String CMD_ACTION_OPEN = "2";
    public static final String CMD_ACTION_6 = "6";
    public static final String CMD_ACTION_100 = "100";

    private static final String[] CMD_ARRAY;
    private static final String[] CMD_NOT_INFRA;

    static {
        CMD_ARRAY = new String[]
                {CMD_ACTION_CLOSE, CMD_ACTION_OPEN, CMD_ACTION_6, CMD_ACTION_100};
        CMD_NOT_INFRA = new String[]
                {CMD_ACTION_CLOSE, CMD_ACTION_OPEN};
    }

    /**
     * 这里只能做空判断
     */
    public static String getCmdAction(String actionId) {
        String lastCmd ;
        if (StringUtil.isNullOrEmpty(actionId))
            lastCmd = CMD_ACTION_OPEN;
        else
            lastCmd = actionId;
        return lastCmd;
    }

    /**
     * 判断ActionId是否可用
     */
    private static boolean isActionIdAvailable(String actionId) {
        boolean isAvailable = false;
        for (int i = 0; i < CMD_ARRAY.length; i++) {
            if (CMD_ARRAY[i].equals(actionId)) {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    }

    /**
     * 确定当前的红外码
     */
    public static String setInfra(String infra, String actionId) {
        boolean actionIdAvailable = isActionIdAvailable(actionId);
        if (!actionIdAvailable) {
            return "";
        }

        for (int i = 0; i < CMD_NOT_INFRA.length; i++) {
            if (CMD_NOT_INFRA[i].equals(actionId)) {
                infra = actionId;
                break;
            }
        }
        return infra;
    }
}
