package cn.leanvision.common.bean;

import java.io.Serializable;

import cn.leanvision.common.util.AirCmdBuilder;
import cn.leanvision.common.util.StringUtil;

/**
 * { "RTN": "A9B0", "infraTypeConfig": { "AUTO": "YY", — 支持自动、可以调温 YN24 —
 * 若不支持调温，则有默认温度 "COLD": "YY", ——制冷 "FAN": "YY", —— 抽湿 "HEAT": "YY", ——制热
 * "WATER": "YY", ——除湿 "_id": "100023", "bigType": "KGHW", "defaultInst": [ ——
 * "<18", "120200", "110800" ], "fanDirection": "YNYYNN", —— 支持风向自动，风向二，风向三
 * "fanSpeed": "YYYY", —— 支持风速自动，风速1，风速2，风速3 "switch": "YY", ——乒乓键
 * ‘YN’表示开关为不同红外指令 "temperature": [ ——温度调节范围 16, 30 ] }, "msg": { "Content":
 * "指令已经执行", "ID": 1 } }
 */
public class InfraConfig implements Serializable {

    private static final long serialVersionUID = 980312038260193772L;

    private String yes = "Y";
    private String no = "N";

    private String AUTO;
    private String COLD;
    private String FAN;
    private String HEAT;
    private String WATER;
    private String bigType;
    private String fanDirection;
    private String fanSpeed;
    private String devSwitch;
    private int maxTemp;
    private int minTemp;

    public void setAUTO(String aUTO) {
        AUTO = aUTO;
    }

    public void setCOLD(String cOLD) {
        COLD = cOLD;
    }

    public void setFAN(String fAN) {
        FAN = fAN;
    }

    public void setHEAT(String hEAT) {
        HEAT = hEAT;
    }

    public void setWATER(String wATER) {
        WATER = wATER;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public void setFanDirection(String fanDirection) {
        this.fanDirection = fanDirection;
    }

    public void setFanSpeed(String fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public void setDevSwitch(String devSwitch) {
        this.devSwitch = devSwitch;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public int getMaxTemp() {
        if (maxTemp == 0)
            this.maxTemp = Integer.parseInt(AirCmdBuilder.STR_CMD_TEMP[AirCmdBuilder.STR_CMD_TEMP.length]);
        return maxTemp;
    }

    public int getMinTemp() {
        if (minTemp == 0)
            this.minTemp = Integer.parseInt(AirCmdBuilder.STR_CMD_TEMP[0]);
        return minTemp;
    }

    /**
     * 是否为乒乓键
     */
    public boolean isSwitchSame() {
        if (StringUtil.isNullOrEmpty(devSwitch))
            return false;
        boolean b = false;
        if (devSwitch.startsWith("YY"))
            b = true;
        return b;
    }

    /**
     * 获取【自动】模式对应的温度
     *
     * @return -2 表示不支持该模式， -1 表示可以调温度,其他数值则表示固定温度不可调
     */
    public int getAutoTemp() {
        if (StringUtil.isNullOrEmpty(AUTO))
            return -1;
        return getModeTemp(AUTO);
    }

    public int getFanTemp() {
        if (StringUtil.isNullOrEmpty(FAN))
            return -1;
        return getModeTemp(FAN);
    }

    public int getWaterTemp() {
        if (StringUtil.isNullOrEmpty(WATER))
            return -1;
        return getModeTemp(WATER);
    }

    /**
     * 解析模式
     */
    private int getModeTemp(String mode) {
        int temp;
        String str1 = mode.substring(0, 1);
        String str2 = mode.substring(1, 2);

        if (yes.equals(str1)) {
            if (yes.equals(str2)) {
                temp = -1;
            } else {
                temp = Integer.parseInt(AUTO.substring(2, AUTO.length()));
            }
        } else {
            temp = -2;
        }
        return temp;
    }

    @Override
    public String toString() {
        return "InfraConfig [AUTO=" + AUTO + ", COLD=" + COLD + ", FAN=" + FAN + ", HEAT=" + HEAT + ", WATER=" + WATER + ", bigType=" + bigType + ", fanDirection=" + fanDirection + ", fanSpeed=" + fanSpeed + ", devSwitch=" + devSwitch + ", maxTemp=" + maxTemp + ", minTemp=" + minTemp + "]";
    }
}
