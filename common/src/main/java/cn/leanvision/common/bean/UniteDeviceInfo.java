package cn.leanvision.common.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import cn.leanvision.common.util.StringUtil;

/********************************
 * Created by lvshicheng on 15/11/10.
 * description
 ********************************/
public class UniteDeviceInfo implements Parcelable {

    private String logoSet;
    private String devType;
    private String panelType;
    private String subWeight;
    private String lastInst;
    private String infraTypeID;
    private String bigType;
    private String infraName;
    private String devTypeID;
    private HashMap<String, String> logoMap;

    public String getDevTypeID() {
        return null == devTypeID ? "" : devTypeID;
    }

    public void setDevTypeID(String devTypeID) {
        this.devTypeID = devTypeID;
    }

    public String getInfraName() {
        return infraName;
    }

    public void setInfraName(String infraName) {
        this.infraName = infraName;
    }

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public String getInfraTypeID() {
        return null == infraTypeID ? "" : infraTypeID;
    }

    public void setInfraTypeID(String infraTypeID) {
        this.infraTypeID = infraTypeID;
    }

    public String getLastInst() {
        return lastInst;
    }

    public void setLastInst(String lastInst) {
        this.lastInst = lastInst;
    }

    public String getLogoSet() {
        return logoSet;
    }

    public HashMap<String, String> getLogoMap() {
        if (logoMap != null)
            return logoMap;
        if (StringUtil.isNotNull(logoSet)) {
            JSONObject jsonObject = JSONObject.parseObject(logoSet);
            if (jsonObject != null && jsonObject.size() > 0) {
                logoMap = new HashMap<>();
                Set<String> keys = jsonObject.keySet();
                Iterator<String> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String value = jsonObject.getString(key);
                    logoMap.put(key, value);
                }
            }
        }
        return logoMap;
    }

    public void setLogoSet(String logoSet) {
        this.logoSet = logoSet;
    }

    public String getPanelType() {
        return panelType;
    }

    public void setPanelType(String panelType) {
        this.panelType = panelType;
    }

    public String getSubWeight() {
        return subWeight;
    }

    public void setSubWeight(String subWeight) {
        this.subWeight = subWeight;
    }

    public UniteDeviceInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (null == bigType)
            return false;
        return bigType.equals(((UniteDeviceInfo) o).bigType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.logoSet);
        dest.writeString(this.devType);
        dest.writeString(this.panelType);
        dest.writeString(this.subWeight);
        dest.writeString(this.lastInst);
        dest.writeString(this.infraTypeID);
        dest.writeString(this.bigType);
        dest.writeString(this.infraName);
        dest.writeString(this.devTypeID);
        dest.writeSerializable(this.logoMap);
    }

    protected UniteDeviceInfo(Parcel in) {
        this.logoSet = in.readString();
        this.devType = in.readString();
        this.panelType = in.readString();
        this.subWeight = in.readString();
        this.lastInst = in.readString();
        this.infraTypeID = in.readString();
        this.bigType = in.readString();
        this.infraName = in.readString();
        this.devTypeID = in.readString();
        this.logoMap = (HashMap<String, String>) in.readSerializable();
    }

    public static final Creator<UniteDeviceInfo> CREATOR = new Creator<UniteDeviceInfo>() {
        public UniteDeviceInfo createFromParcel(Parcel source) {
            return new UniteDeviceInfo(source);
        }

        public UniteDeviceInfo[] newArray(int size) {
            return new UniteDeviceInfo[size];
        }
    };
}
