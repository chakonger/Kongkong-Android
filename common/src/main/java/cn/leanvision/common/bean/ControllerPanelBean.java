package cn.leanvision.common.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lvshicheng
 * @date 2015年09月25日11:45:26
 * @description 控制面板
 */
public class ControllerPanelBean implements Parcelable {
    private String _id;
    private int keyRow;
    private int keyCol;
    private String timer;
    /**
     * 每一行对应的列数 [2,4]
     */
    private int[] keyRowNum;
    private String meter;
    private String panelType;
    private int keyCount;
    private String[] timerCom;
    private HashMap<String, ItemPanel> keysSet;
    private ArrayList<ItemPanel> timerList;
    private List<Integer> keyList;
    /**
     * 按钮ICON子路径
     */
//    private String imgBaseUrl;
    private String dirURL;
    private HashMap<String, String> logoSet;

    public HashMap<String, String> getLogoSet() {
        return logoSet;
    }

    public void setLogoSet(HashMap<String, String> logoSet) {
        this.logoSet = logoSet;
    }

    public ArrayList<ItemPanel> getTimerList() {
        return timerList;
    }

    public void setTimerList(ArrayList<ItemPanel> timerList) {
        this.timerList = timerList;
    }

    public String getDirURL() {
        return dirURL;
    }

    public void setDirURL(String dirURL) {
        this.dirURL = dirURL;
    }

    public List<Integer> getKeyList() {
        return keyList;
    }

    public void setKeyList(List<Integer> keyList) {
        this.keyList = keyList;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_id() {
        return _id;
    }

    public int getKeyRow() {
        return keyRow;
    }

    public void setKeyRow(int keyRow) {
        this.keyRow = keyRow;
    }

    public int getKeyCol() {
        return keyCol;
    }

    public void setKeyCol(int keyCol) {
        this.keyCol = keyCol;
    }

    public int getKeyCount() {
        return keyCount;
    }

    public void setKeyCount(int keyCount) {
        this.keyCount = keyCount;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public int[] getKeyRowNum() {
        return keyRowNum;
    }

    public void setKeyRowNum(int[] keyRowNum) {
        this.keyRowNum = keyRowNum;
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public String getPanelType() {
        return panelType;
    }

    public void setPanelType(String panelType) {
        this.panelType = panelType;
    }

    public String[] getTimerCom() {
        return timerCom;
    }

    public void setTimerCom(String[] timerCom) {
        this.timerCom = timerCom;
    }

    public HashMap<String, ItemPanel> getKeysSet() {
        return keysSet;
    }

    public void setKeysSet(HashMap<String, ItemPanel> keysSet) {
        this.keysSet = keysSet;
    }

    public static class ItemPanel implements Parcelable {

        private String[] inst;
        private String tag;
        private int instCount;
        private String[] text;
        /**
         * 用于标示当前取到第几个命令
         */
        private int index = 0;
        /**
         * Y 表示该指令支持定时 N 表示该指令不支持
         */
        private String timer;
        private String img;
        private String bkg;
        /**
         * 目前之定义了两种类型：
         * 1.key 当做简单的按键
         * 2.url 跳转指定的url链接
         */
        private String type;
        private String url;

        public String getBkg() {
            return bkg;
        }

        public void setBkg(String bkg) {
            this.bkg = bkg;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getTimer() {
            return timer;
        }

        public void setTimer(String timer) {
            this.timer = timer;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getInst() {
            if (inst == null || inst.length == 0)
                return "";
            String result = inst[index % inst.length];
            index++;
            return result;
        }

        public String getInst(int index) {
            if (index > 0 && index < inst.length)
                return inst[index];
            else
                return "";
        }

        public void setInst(String[] inst) {
            this.inst = inst;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public int getInstCount() {
            return instCount;
        }

        public void setInstCount(int instCount) {
            this.instCount = instCount;
        }

        public String getText() {
            if (text == null)
                return "";
            StringBuilder sbr = new StringBuilder();
            for (int i = 0; i < text.length; i++) {
                if (i > 0)
                    sbr.append("/");
                sbr.append(text[i]);
            }
            return sbr.toString();
        }

        public String getText(int index) {
            if (index > 0 && index < text.length)
                return text[index];
            else
                return "";
        }

        public void setText(String[] text) {
            this.text = text;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringArray(this.inst);
            dest.writeString(this.tag);
            dest.writeInt(this.instCount);
            dest.writeStringArray(this.text);
            dest.writeInt(this.index);
            dest.writeString(this.timer);
            dest.writeString(this.img);
            dest.writeString(this.bkg);
            dest.writeString(this.type);
            dest.writeString(this.url);
        }

        public ItemPanel() {
        }

        protected ItemPanel(Parcel in) {
            this.inst = in.createStringArray();
            this.tag = in.readString();
            this.instCount = in.readInt();
            this.text = in.createStringArray();
            this.index = in.readInt();
            this.timer = in.readString();
            this.img = in.readString();
            this.bkg = in.readString();
            this.type = in.readString();
            this.url = in.readString();
        }

        public static final Creator<ItemPanel> CREATOR = new Creator<ItemPanel>() {
            public ItemPanel createFromParcel(Parcel source) {
                return new ItemPanel(source);
            }

            public ItemPanel[] newArray(int size) {
                return new ItemPanel[size];
            }
        };
    }

    public ControllerPanelBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this._id);
        dest.writeInt(this.keyRow);
        dest.writeInt(this.keyCol);
        dest.writeString(this.timer);
        dest.writeIntArray(this.keyRowNum);
        dest.writeString(this.meter);
        dest.writeString(this.panelType);
        dest.writeInt(this.keyCount);
        dest.writeStringArray(this.timerCom);
        dest.writeSerializable(this.keysSet);
        dest.writeTypedList(timerList);
        dest.writeList(this.keyList);
        dest.writeString(this.dirURL);
        dest.writeSerializable(this.logoSet);
    }

    protected ControllerPanelBean(Parcel in) {
        this._id = in.readString();
        this.keyRow = in.readInt();
        this.keyCol = in.readInt();
        this.timer = in.readString();
        this.keyRowNum = in.createIntArray();
        this.meter = in.readString();
        this.panelType = in.readString();
        this.keyCount = in.readInt();
        this.timerCom = in.createStringArray();
        this.keysSet = (HashMap<String, ItemPanel>) in.readSerializable();
        this.timerList = in.createTypedArrayList(ItemPanel.CREATOR);
        this.keyList = new ArrayList<Integer>();
        in.readList(this.keyList, List.class.getClassLoader());
        this.dirURL = in.readString();
        this.logoSet = (HashMap<String, String>) in.readSerializable();
    }

    public static final Creator<ControllerPanelBean> CREATOR = new Creator<ControllerPanelBean>() {
        public ControllerPanelBean createFromParcel(Parcel source) {
            return new ControllerPanelBean(source);
        }

        public ControllerPanelBean[] newArray(int size) {
            return new ControllerPanelBean[size];
        }
    };
}
