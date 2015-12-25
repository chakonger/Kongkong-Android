package cn.leanvision.common.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/********************************
 * Created by lvshicheng on 15/12/22.
 * description 单个设备
 ********************************/
public class DeviceInfo implements Parcelable {

    private String devStatus;
    private String devID;
    private String token;
    private String lastInst;
    private String devName;
    private String seed;
    private String devType;
    private String bigType;
    private List<UniteDeviceInfo> unitedevice;

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public String getDevID() {
        return devID;
    }

    public void setDevID(String devID) {
        this.devID = devID;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevStatus() {
        return devStatus;
    }

    public void setDevStatus(String devStatus) {
        this.devStatus = devStatus;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public String getLastInst() {
        return lastInst;
    }

    public void setLastInst(String lastInst) {
        this.lastInst = lastInst;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<UniteDeviceInfo> getUnitedevice() {
        return unitedevice;
    }

    public void setUnitedevice(List<UniteDeviceInfo> unitedevice) {
        this.unitedevice = unitedevice;
    }

    public List<UniteDeviceInfo> getUniteList() {
        if (null == unitedevice)
            unitedevice = new ArrayList<>();
        if (unitedevice.isEmpty()) {
            //TODO 如果返回列表是空，则取用本体的状态
            UniteDeviceInfo fpb = new UniteDeviceInfo();
            fpb.setDevType(devType);
            fpb.setSubWeight("");
            fpb.setPanelType("");
            fpb.setLastInst("");
            fpb.setInfraTypeID(null);
            fpb.setBigType(bigType);
            fpb.setLogoSet("");
            unitedevice.add(fpb);
        }
        return unitedevice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.devStatus);
        dest.writeString(this.devID);
        dest.writeString(this.token);
        dest.writeString(this.lastInst);
        dest.writeString(this.devName);
        dest.writeString(this.seed);
        dest.writeString(this.devType);
        dest.writeTypedList(unitedevice);
        dest.writeString(this.bigType);
    }

    public DeviceInfo() {
    }

    protected DeviceInfo(Parcel in) {
        this.devStatus = in.readString();
        this.devID = in.readString();
        this.token = in.readString();
        this.lastInst = in.readString();
        this.devName = in.readString();
        this.seed = in.readString();
        this.devType = in.readString();
        this.unitedevice = in.createTypedArrayList(UniteDeviceInfo.CREATOR);
        this.bigType = in.readString();
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        public DeviceInfo createFromParcel(Parcel source) {
            return new DeviceInfo(source);
        }

        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };
}
