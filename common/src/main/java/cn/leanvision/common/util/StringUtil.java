package cn.leanvision.common.util;

public class StringUtil {

	public static boolean isNullOrEmpty(String str) {
		boolean result = false;
		if (null == str || "".equals(str.trim())) {
			result = true;
		}
		return result;
	}

	public static boolean isNotNull(String str){
		return !isNullOrEmpty(str);
	}
}
