package cn.leanvision.normalkongkong;

import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;

/********************************
 * Created by lvshicheng on 15/12/21.
 * description
 ********************************/
public class CommonUtil {

    public static String formatUrl(String subUrl) {
        return String.format(Locale.CHINA, "%s/%s", Constants.SERVER_ADDRESS, subUrl);
    }

    /**
     * 绑定时生成五位随机码 五位密码范围 01001->65534
     */
    public static String getDeviceRandomNum() {
        Random random = new Random();
        int nextInt = random.nextInt(64533) + 1001; // [0, 64533)
        if (nextInt < 10000)
            return "0" + nextInt;
        else
            return "" + nextInt;
    }

    public static JSONObject getCommonRequest() {
        JSONObject jsonObject = new JSONObject();
        String sessionID = SharedPrefHelper.getInstance().getSessionID();
        jsonObject.put("sessionID", sessionID);
        return jsonObject;
    }

    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }
}
