package cn.leanvision.normalkongkong;

/********************************
 * Created by lvshicheng on 15/11/19.
 * description
 ********************************/
public class Constants {
    /**
     * 网络请求缓存开关
     */
    public static final boolean CACHE_ENABLE = true;
    /**
     * 网络请求缓存时长 （unit S）
     */
    public static final int CACHE_TIME = 5 * 60;
    /**
     * 测试服务器地址
     * http://ss1.chakonger.net.cn
     */
    public static final String SERVER_ADDRESS = "http://ss1.chakonger.net.cn";
    /**
     * 替换成本公司分配到的APP_ID
     */
    public static final String APP_ID = "leanvision";
    /**
     * 绑定是传给插座的服务器地址，暂不支持域名
     */
    public static final String BIND_ADDRESS = "118.192.76.159";
    public static final String BIND_PORT = "80";

    public static final String ERROR_CODE_SUCCEED = "0";
    /**
     * 设备状态
     */
    public static final String DEV_TYPE_OFFLINE = "A002";
    public static final String DEV_TYPE_ONLINE = "A003";
    public static final String DEV_TYPE_WORK = "A004";

    /******************
     * 所有请求后缀 - START
     ******************/
    /**
     * 获取通知地址
     */
    public static final String SUF_GET_PUSH_ADDRESS = "web/getpushaddress";
    /**
     * 查询推送消息
     */
    public static final String SUF_GET_PUSH_EVENT = "web/getpushevent";
    /**
     * 发起绑定
     */
    public static final String SUF_POST_BIND = "web/devicebind";
    /**
     * 控制面板以及红外支持能力查询
     */
    public static final String SUF_INFRA_QUERY = "web/infratypeability";
    /**
     * 删除设备
     */
    public static final String SUF_DEVICE_REMOVE = "web/deviceremove";
    /**
     * 单设备查询
     */
    public static final String SUF_DEVICE_QUERY = "web/deviceqry";
    /**
     * 控制设备
     */
    public static final String SUF_DEVICE_CONTROL = "web/action?actionID=%s&inst=%s&token=%s&infraTypeID=%s";

    /******************
     * 所有请求后缀 - END
     ******************/

    /**
     * 闹钟定时广播
     */
    public static final String LV_ACTION_REPEATE = "cn.leanvision.repeate";

    /**
     * 设备状态变更推送
     */
    public static final String PUSH_STATUS = "N0A0";
    public static final String BROADCAST_STATUS = "cn.leanvision.normalkongkong.status";
    /**
     * 设备被他人绑定推送
     */
    public static final String PUSH_BOUNDED = "N2A0";
    public static final String BROADCAST_BOUNDED = "cn.leanvision.normalkongkong.bounded";
    /**
     * 同步红外推送
     */
    public static final String PUSH_INFRA_SYNC = "N4A0";
    public static final String BROADCAST_INFRA_SYNC = "cn.leanvision.normalkongkong.infrasync";
    /**
     * 设备控制结果推送
     */
    public static final String PUSH_CONTROL_RESULT = "N5A0";
    public static final String BROADCAST_CONTROL_RESULT = "cn.leanvision.normalkongkong.controlresult";
    /**
     * 新增红外推送
     */
    public static final String PUSH_NEW_INFRA_TYPE = "N1A1";
    public static final String BROADCAST_INFRA_TYPE = "cn.leanvision.normalkongkong.infratype";
    /**
     * 新增设备推送
     */
    public static final String PUSH_BIND_SUCCEED = "N6A0";
    public static final String BROADCAST_BIND_SUCCEED = "cn.leanvision.normalkongkong.bindsucceed";

}
