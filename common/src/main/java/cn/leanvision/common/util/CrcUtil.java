package cn.leanvision.common.util;

import java.security.MessageDigest;

public class CrcUtil {

    public static String MD5(String source) {
        String resultHash = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(source.getBytes("UTF-8"));
            byte[] result = md5.digest();
            StringBuffer buf = new StringBuffer(result.length * 2);
            for (int i = 0; i < result.length; i++) {
                int intVal = result[i] & 0xff;
                if (intVal < 0x10) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(intVal));
            }
            resultHash = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultHash.toString();
    }
}
