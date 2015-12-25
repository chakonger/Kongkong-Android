package cn.leanvision.normalkongkong.util;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import cn.leanvision.common.util.LogUtil;

/**
 * @author lvshicheng
 * @date 2015-2-4 11:35:36
 * @description 简单的一个http请求，主要是为了方便代码组织
 */
public class HttpRequestUtil {
    /**
     * 获取返回超时
     */
    private static final int HTTP_READ_TIMEOUT = 30 * 1000;
    /**
     * 请求连接超时
     */
    private static final int HTTP_CONNECT_TIMEOUT = 30 * 1000;

    /*************************
     * POST Method
     ************************/
    /**
     * 普通POST请求、指定地址
     */
    public static String requestPost(String request, String url) {
        LogUtil.log("requestContent = " + request);
        String urlString = url;
        LogUtil.log("requestUrl = " + urlString);
        return openConnect(request, urlString);
    }

    private static String openConnect(String request, String urlString) {
        return openConnect(request, urlString, -1);
    }

    /**
     * 带参数的post请求
     */
    private static String openConnect(String request, String urlString, int timeOut) {
        String result;
        StringBuffer resultString = new StringBuffer();
        PrintWriter out;
        BufferedReader in;
        URL realUrl;
        try {
            realUrl = new URL(urlString);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            if (timeOut == -1) {
                conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
                conn.setReadTimeout(HTTP_READ_TIMEOUT);
            } else {
                conn.setConnectTimeout(timeOut);
                conn.setReadTimeout(timeOut);
            }
            // 设置通用的请求属性
            conn.setRequestProperty("Content-type", "text/json");
            conn.setRequestProperty("connection", "Keep-Alive");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            out.print(request);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                resultString.append("\n" + line);
            }
            result = resultString.toString();
            LogUtil.log("result = " + result);
            JSONObject parseObject = JSONObject.parseObject(result);
            if (parseObject == null)
                result = null;
        } catch (MalformedURLException e) { // 请求地址错误
            e.printStackTrace();
            result = null;
        } catch (SocketTimeoutException e) {
            result = null;
        } catch (IOException e) { // 打开连接错误
            e.printStackTrace();
            result = null;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /*************************
     * GET Method
     ************************/
    private static String openConnectGet(String urlString, int timeOut) {
        String result;
        StringBuffer resultString = new StringBuffer();
        BufferedReader in;
        URL realUrl;
        try {
            realUrl = new URL(urlString);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            if (timeOut == -1) {
                conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT / 3);
                conn.setReadTimeout(HTTP_READ_TIMEOUT / 3);
            } else if (timeOut == -2) {
                conn.setConnectTimeout(HTTP_READ_TIMEOUT * 2 * 5);
                conn.setReadTimeout(HTTP_READ_TIMEOUT * 2 * 5);
            } else {
                conn.setConnectTimeout(timeOut);
                conn.setReadTimeout(timeOut);
            }
            // 设置通用的请求属性
            conn.setRequestProperty("Content-type", "application/json");
            // conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                resultString.append("\n" + line);
            }
            result = URLDecoder.decode(resultString.toString(), "UTF-8");
            LogUtil.e("ResultConnGet = " + result);
            JSONObject parseObject = JSONObject.parseObject(result);
            if (parseObject == null)
                result = null;
        } catch (MalformedURLException e) { // 请求地址错误
            e.printStackTrace();
            result = null;
        } catch (IOException e) { // 打开连接错误
            e.printStackTrace();
            result = null;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * 类似GET请求
     */
    private static String openConnectGet(String url) {
        return openConnectGet(url, -1);
    }

    public static String requestGetForUser(String url) {
        return openConnectGet(url, -2);
    }
}
