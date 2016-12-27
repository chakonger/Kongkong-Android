package cn.com.leanvision.libbound.bluetooth;

/********************************
 * Created by lvshicheng on 2016/10/28.
 ********************************/
public enum ErrorCode {

    /**
     * 蓝牙不可用
     */
    BT_UNAVAILABLE(0),
    /**
     * 蓝牙未开启
     */
    BT_CLOSED(1),
    /**
     * 未成功连接蓝牙
     */
    BT_OVER(2);

    private final int code;

    public int getCode() {
        return code;
    }

    ErrorCode(int i) {
        this.code = i;
    }
}
