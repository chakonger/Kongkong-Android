package cn.leanvision.normalkongkong.request;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Cache;
import com.android.volley.ParseError;
import com.android.volley.Response;

import cn.leanvision.common.util.CrcUtil;
import cn.leanvision.normalkongkong.framework.request.LvBaseRequest;
import cn.leanvision.normalkongkong.response.InfraConfigResponse;
import cn.leanvision.normalkongkong.response.parser.InfraConfigParser;

/********************************
 * Created by lvshicheng on 15/10/28.
 * description
 ********************************/
public class ControlPanelRequest extends LvBaseRequest<InfraConfigResponse> {

    public ControlPanelRequest(String url, String body, Response.Listener<InfraConfigResponse> listener, Response.ErrorListener errorListener) {
        super(url, body, listener, errorListener);
    }

    @Override
    protected Response<InfraConfigResponse> subParseNetworkResponse(JSONObject json, Cache.Entry entry) {
        try {
            InfraConfigParser infraConfigParser = new InfraConfigParser();
            InfraConfigResponse response = infraConfigParser.parse(json.toString());
            return Response.success(response, entry);
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public String getCacheKey() {
        String realBody = getRealBody();
        JSONObject jsonObject = JSONObject.parseObject(realBody);
        return CrcUtil.MD5(String.format("ControlPanelCache cache key is %s", jsonObject.getString("infraTypeID")));
    }
}
