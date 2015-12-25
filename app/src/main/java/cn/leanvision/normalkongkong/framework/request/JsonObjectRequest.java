package cn.leanvision.normalkongkong.framework.request;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.VolleyError;

/********************************
 * Created by lvshicheng on 15/11/19.
 * description
 ********************************/
public class JsonObjectRequest extends LvBaseRequest<JSONObject> {

    public JsonObjectRequest(String url, String body, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, body, listener, errorListener);
    }

    public JsonObjectRequest(int method, String url, String body, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, body, listener, errorListener);
    }

    @Override
    protected Response<JSONObject> subParseNetworkResponse(JSONObject jsonObject, Cache.Entry entry) {
        try {
            return Response.success(jsonObject, entry);
        } catch (Exception e) {
            return Response.error(new VolleyError());
        }
    }
}
