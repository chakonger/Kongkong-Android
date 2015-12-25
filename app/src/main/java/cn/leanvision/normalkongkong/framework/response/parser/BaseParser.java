package cn.leanvision.normalkongkong.framework.response.parser;

import com.alibaba.fastjson.JSONObject;

import cn.leanvision.normalkongkong.framework.response.BaseResponse;

public abstract class BaseParser<T extends BaseResponse> {
	public static final String ERROR_CODE = "errcode";
	public static final String MSG = "errmsg";

	public abstract T parse(String paramString); 
	
	public void resultBaseParser(BaseResponse response, JSONObject mJsonObject){
		response.RTN = mJsonObject.getString(ERROR_CODE);
		response.msg = mJsonObject.getString(MSG);
	}
}
