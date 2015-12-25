package cn.leanvision.normalkongkong.framework.request;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import cn.leanvision.common.util.LogUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.Constants;

/********************************
 * Created by lvshicheng on 15/10/16.
 * description
 ********************************/
public abstract class LvBaseRequest<T> extends Request<T> {

    protected Response.Listener<T> listener;
    protected String desc;
    private String body;
    private Priority priority;

    public LvBaseRequest(String url, String body, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(Method.POST, url, body, listener, errorListener, Priority.NORMAL);
    }

    public LvBaseRequest(String body, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(Method.POST, Constants.SERVER_ADDRESS, body, listener, errorListener, Priority.NORMAL);
    }

    public LvBaseRequest(int method, String url, String body, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, body, listener, errorListener, Priority.NORMAL);
    }

    public LvBaseRequest(String url, String body, Response.Listener<T> listener, Response.ErrorListener errorListener, Priority priority) {
        this(Method.POST, url, body, listener, errorListener, priority);
    }

    public LvBaseRequest(int method, String url, String body, Response.Listener<T> listener, Response.ErrorListener errorListener, Priority priority) {
        super(method, url, errorListener);

        LogUtil.log(getClass(), String.format("【RequestUrl = %s】", url));

        this.listener = listener;
        this.body = body;
        this.priority = priority;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (body == null)
            return null;
        if (StringUtil.isNullOrEmpty(desc))
            desc = "Volley Request";
        LogUtil.log(getClass(), String.format("【%s】Request : %s", desc, body));
        return body.getBytes();
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRealBody() {
        return body;
    }

    @Override
    public String getCacheKey() {
        return "";
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public void setCacheTime(int cacheTime) {
        super.setCacheTime(cacheTime);
    }

    public void setDescription(String desc) {
        this.desc = desc;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            if (StringUtil.isNullOrEmpty(desc))
                desc = "Volley Response";
            LogUtil.log(getClass(), String.format("【%s】Response : %s", desc, json));

            final Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);
            JSONObject jsonObject = JSONObject.parseObject(json);
            //FIXME 这里要添加处理全局异常返回，如sessionID失效
            return subParseNetworkResponse(jsonObject, entry);
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    protected abstract Response<T> subParseNetworkResponse(JSONObject json, Cache.Entry entry);
}
