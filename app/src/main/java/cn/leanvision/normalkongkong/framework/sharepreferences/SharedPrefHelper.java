package cn.leanvision.normalkongkong.framework.sharepreferences;

import android.content.Context;
import android.content.SharedPreferences;

import cn.leanvision.common.util.EncryptionUtil;
import cn.leanvision.normalkongkong.LvApplication;

public class SharedPrefHelper {
    /**
     * SharedPreferences的名字
     */
    private static final String           SP_FILE_NAME     = "normal_kong";
    private static       SharedPrefHelper sharedPrefHelper = null;
    private static SharedPreferences spf;

    public static synchronized SharedPrefHelper getInstance() {
        if (null == sharedPrefHelper) {
            sharedPrefHelper = new SharedPrefHelper();
        }
        return sharedPrefHelper;
    }

    private SharedPrefHelper() {
        spf = LvApplication.getInstance().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserName(String userName) {
        try {
            spf.edit().putString("a", EncryptionUtil.encode(userName)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserName() {
        String a = spf.getString("a", "");
        try {
            return EncryptionUtil.decode(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }

    public void savePwd(String pwd) {
        try {
            spf.edit().putString("b", EncryptionUtil.encode(pwd)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPwd() {
        String b = spf.getString("b", "");
        try {
            return EncryptionUtil.decode(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    public void saveSessionID(String sessionID) {
        spf.edit().putString("c", sessionID).apply();
    }

    public String getSessionID() {
        return spf.getString("c", "");
    }

    public void saveWifiPwd(String wifiPWd) {
        try {
            spf.edit().putString("d", EncryptionUtil.encode(wifiPWd)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getWifiPwd() {
        String d = spf.getString("d", "");
        try {
            return EncryptionUtil.decode(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }

    public void setBackgroundTaskStop(boolean isStop) {
        spf.edit().putBoolean("e", isStop).apply();
    }

    public boolean isBackGroundTaskStop() {
        return spf.getBoolean("e", false);
    }

    public void saveBackGroundGetUrl(String url) {
        try {
            spf.edit().putString("f", EncryptionUtil.encode(url)).apply();
            spf.edit().putLong("g", System.currentTimeMillis()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBackGroundGetUrl() {
        long saveTime = spf.getLong("g", 0);
        //URL是24小时失效
        if (System.currentTimeMillis() - saveTime > 23 * 60 * 60 * 1000)
            return null;
        else {
            String f = spf.getString("f", "");
            try {
                return EncryptionUtil.decode(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return f;
        }
    }
}
