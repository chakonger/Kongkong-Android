package cn.com.leanvision.libbound.bluetooth;

/********************************
 * Created by lvshicheng on 2016/10/28.
 ********************************/
public interface BTCallback {

    void btError(ErrorCode errorCode);

    void btSuccess();

    void btStartChat();

    void btProgress(int step);

}
