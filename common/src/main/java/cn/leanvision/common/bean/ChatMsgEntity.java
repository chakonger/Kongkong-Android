package cn.leanvision.common.bean;


public class ChatMsgEntity {

    private int msgId;
    private String mineOpenFireId;
    private String fid;
    private String name;//
    private String date;// 日期
    private String text;// 内容
    private String msgType;// 消息类型  (添加中间显示类型 middle add by lsc 2014-12-29 16:09:10  -- 如：3_1)
    private boolean isComMeg = true;// true朋友消息，flase自己的消息
    private String nativePath;// 图片和音频需要保存在本地
    private boolean isShowProgressbar;// 是否显示正在发送的进度条
    private boolean isSendCompleted;// 发送是否成功
    private String head_url;
    private boolean isPlayVoiceAnim;// 是否应该打开播放声音的动画
    private String timeMillis;// 语音的时长，秒级

    private String devType;// 设备类型
    private String devStatus;// 设备状态
    private String bigType;
    private String devIconUrl;

    // 用于显示中间内容标志
//	private boolean isMiddleMsg = false;
    // 显示中间消息类型，用于跳转用
//	private int middleMsgType;

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public String getDevTypeID() {
        return devIconUrl == null ? "" : devIconUrl;
    }

    public void setDevIconUrl(String devIconUrl) {
        this.devIconUrl = devIconUrl;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public String getDevStatus() {
        return devStatus;
    }

    public void setDevStatus(String devStatus) {
        this.devStatus = devStatus;
    }

    public String getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(String timeMillis) {
        this.timeMillis = timeMillis;
    }

    public boolean isPlayVoiceAnim() {
        return isPlayVoiceAnim;
    }

    public void setPlayVoiceAnim(boolean isPlayVoiceAnim) {
        this.isPlayVoiceAnim = isPlayVoiceAnim;
    }

    public String getMineOpenFireId() {
        return mineOpenFireId;
    }

    public void setMineOpenFireId(String mineOpenFireId) {
        this.mineOpenFireId = mineOpenFireId;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getHead_url() {
        return head_url;
    }

    public void setHead_url(String head_url) {
        this.head_url = head_url;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isComMsg() {
        return isComMeg;
    }

    public void setComMsg(boolean isComMsg) {
        isComMeg = isComMsg;
    }

    public String getNativePath() {
        return nativePath;
    }

    public void setNativePath(String nativePath) {
        this.nativePath = nativePath;
    }

    public boolean isShowProgressbar() {
        return isShowProgressbar;
    }

    public void setShowProgressbar(boolean isShowProgressbar) {
        this.isShowProgressbar = isShowProgressbar;
    }

    public boolean isSendCompleted() {
        return isSendCompleted;
    }

    public void setSendCompleted(boolean isSendCompleted) {
        this.isSendCompleted = isSendCompleted;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        ChatMsgEntity cme = (ChatMsgEntity) o;
        return this.msgId == cme.msgId;
    }
}
