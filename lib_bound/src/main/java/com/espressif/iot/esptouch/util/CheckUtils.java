package com.espressif.iot.esptouch.util;

import android.os.Looper;

/********************************
 * Created by lvshicheng on 16/9/19.
 * <p>
 * 校验非空的地方
 ********************************/
public class CheckUtils {

    public static void checkNotNull(Object obj, String... args) {
        if (obj == null) {
            checkHandle(args);
        }
    }

    public static void checkNotOnMainThread(String... args) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            checkHandle(args);
        }
    }

    private static void checkHandle(String... args) {
        if (args != null && args.length > 0)
            throw new RuntimeException(args[0]);
        else
            throw new RuntimeException();
    }
}
