package cn.leanvision.normalkongkong.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.mime.VolleyHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.leanvision.common.util.LogUtil;
import cn.leanvision.common.util.NetUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.CommonUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.framework.request.JsonObjectRequest;
import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;
import cn.leanvision.normalkongkong.util.HttpRequestUtil;

/**
 * @author lvshicheng
 * @date 2015年12月22日10:03:03
 * @description 后台常驻服务
 */
public class LvKongCoreService extends Service {
  /**
   * 后台持续get线程
   */
  private static BackGetThread backRunnable = null;

  private ExecutorService mFixedThreadPool;

  public CoreBinder mBinder;

  private String requestTag = LvKongCoreService.class.getSimpleName();
  /**
   * 获取推送消息的请求
   */
  private JsonObjectRequest request;

  private LocalBroadcastManager manager;

  public LvKongCoreService() {
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mFixedThreadPool = Executors.newFixedThreadPool(1);

    manager = LocalBroadcastManager.getInstance(getApplicationContext());
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (mBinder == null)
      mBinder = new CoreBinder();
    return mBinder;
  }

  public class CoreBinder extends Binder {
    /**
     * 初始化后台GET任务
     */
    public synchronized void initBackGetThread(boolean mustReBuild) {
      LogUtil.log(getClass(), "1111111111");
      if (SharedPrefHelper.getInstance().isBackGroundTaskStop()) {
        if (backRunnable != null) {
          backRunnable.cancel();
          backRunnable = null;
        }

        if (mFixedThreadPool != null) {
          mFixedThreadPool.shutdownNow();
          mFixedThreadPool = null;
        }
        return;
      }

      LogUtil.log(getClass(), "2222222222");
      if (backRunnable != null) {
        if (!backRunnable.currentAccount.equals(SharedPrefHelper.getInstance().getUserName())) {
          // 不相等表示切换用户了, 必须重新开启get
          mustReBuild = true;
          request = null;
        }
      }

      LogUtil.log(getClass(), "333333333");
      if (mustReBuild) { // 强制的话则需要清除所有数据

        if (backRunnable != null) {
          backRunnable.cancel();
          backRunnable = null;
        }

        if (mFixedThreadPool != null) {
          mFixedThreadPool.shutdownNow();
          mFixedThreadPool = null;
        }
      }

      LogUtil.log(getClass(), "444444444");
      if (mFixedThreadPool == null) {
        mFixedThreadPool = Executors.newSingleThreadExecutor();
      }

      if (backRunnable == null) {
        backRunnable = new BackGetThread();
      }

      if (backRunnable.isRunning) {
        boolean checkStatues = backRunnable.checkStatues();
        if (checkStatues) {
          initBackGetThread(true);
        }
        return;
      }

      LogUtil.log(getClass(), "555555555");
      backRunnable.stopBack = false;
      mFixedThreadPool.execute(backRunnable);
    }
  }

  private class BackGetThread implements Runnable {
    private long    id             = -1;
    private boolean isRunning      = false;
    private boolean stopBack       = false;
    public  int     position       = 0;
    public  long    lastUpdateTime = 0;
    public String currentAccount;

    public BackGetThread() {
      this.currentAccount = SharedPrefHelper.getInstance().getUserName();
      id = System.currentTimeMillis();
      lastUpdateTime = System.currentTimeMillis();
      isRunning = false;
      start();
    }

    /**
     * 取消任务
     */
    public void cancel() {
      stopBack = true;
    }

    /**
     * 开启任务
     */
    public void start() {
      stopBack = false;
    }

    public boolean checkStatues() {
      if (System.currentTimeMillis() - lastUpdateTime > 5 * 60 * 1000) {
        // 如果保持一个状态五分钟，则表示需要重新开始新线程
        return true;
      }
      return false;
    }

    @Override
    public void run() {
      LogUtil.log(getClass(), "66666666666");
      setPosition(0);
      while (!stopBack) {
        LogUtil.log(getClass(), "7777777777777");
        setPosition(2);
        if (!NetUtil.isNetWorkAvailable(getApplicationContext())) {
          stopBack = true;
          break;
        }
        setPosition(3);
        isRunning = true;
        setPosition(4);

        SharedPrefHelper sph = SharedPrefHelper.getInstance();
        //检查GET地址的有效性
        String backGroundGetUrl = sph.getBackGroundGetUrl();
        LogUtil.log(getClass(), "backGroundGetUrl : " + backGroundGetUrl);
        if (StringUtil.isNullOrEmpty(backGroundGetUrl)) {
          //重新获取URL
          //http://HOST_ADDRESS/web/getpushaddress post: {" sessionID ":"xxxxxxxxxxxxxxx"}
          String body = String.format("{\"sessionID\":\"%s\"}", sph.getSessionID());
          String url = CommonUtil.formatUrl(Constants.SUF_GET_PUSH_ADDRESS);
          String result = HttpRequestUtil.requestPost(body, url);
          LogUtil.log("result: " + result);
          //{"errcode":0,"errmsg":"ok"," address":"http://xxxx/xx"}
          if (null != result) {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String address = jsonObject.getString("address");
            if (StringUtil.isNotNull(address)) {
              sph.saveBackGroundGetUrl(address);
              backGroundGetUrl = address;
            } else {
              break;
            }
          } else {
            break;
          }
        }

        LogUtil.log("backGroundGetUrl: " + backGroundGetUrl);
        backGroundGetUrl = backGroundGetUrl.replace("ss1.chakonger.net.cn", Constants.BIND_ADDRESS);
        String requestGetForDevice = HttpRequestUtil.requestGetForUser(backGroundGetUrl);
        setPosition(5);
        LogUtil.e(getClass(), "BackGetThread return : " + id);
        if (!StringUtil.isNullOrEmpty(requestGetForDevice)) {
          if (stopBack) // 如果停止就没有执行下去的意义
          {
            break;
          }
          requestSystemMsg();
        } else {
          // 如果返回空，则说明异常，需要(～﹃～)~zZ
          setPosition(9);
          try {
            Thread.sleep(5 * 1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        setPosition(7);
      }
      setPosition(8);
      isRunning = false;
      LogUtil.log("------------------GET Service  " + id + "------------- stop");
    }

    public void setPosition(int position) {
      if (this.position == position) {
        return;
      }
      this.position = position;
      lastUpdateTime = System.currentTimeMillis();
    }
  }

  /*************************************
   * 从服务器获取系统消息 - 一个小时取一次 - START
   *************************************/
  public void requestSystemMsg() {
    if (!NetUtil.isNetWorkAvailable(getApplicationContext())) {
      return;
    }
    Context context = getApplicationContext();
    JsonObjectRequest request = makeSysRequest();
    VolleyHelper.addRequest(context, request, requestTag);
  }

  /**
   * {"errcode": 0, "errmsg": "success", "event": [{"upTime": "20151223152319", "CMD": "N4A0", "infraTypeID": "100001", "infraInst": "110E00", "bigType": "KGHW", "devType": "\u7a7a\u8c03", "devID": "311411035510927"}]}
   * <p/>
   * {"errcode": 0, "errmsg": "success", "event": [{"postTime": "20151223152400", "uptime": "20151223152400", "devID": "311411035510927", "res": "0", "infraTypeID": "", "CMD": "N5A0", "bigType": "KG", "devTypeID": "", "delay": "0", "inst": "", "actionID": "0", "IP": "106.38.169.206"}]}
   */
  public JsonObjectRequest makeSysRequest() {
    if (request == null) {
      Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject responseJson) {
          //TODO 解析返回推送消息
          JSONArray events = responseJson.getJSONArray("event");
          if (null == events || events.isEmpty())
            return;
          for (int i = 0; i < events.size(); i++) {
            JSONObject jsonObject = events.getJSONObject(i);
            String cmd = jsonObject.getString("CMD");
            Intent intent = null;
            if (Constants.PUSH_STATUS.equals(cmd)) {
              intent = new Intent(Constants.BROADCAST_STATUS);
            } else if (Constants.PUSH_BOUNDED.equals(cmd)) {
              intent = new Intent(Constants.BROADCAST_BOUNDED);
            } else if (Constants.PUSH_INFRA_SYNC.equals(cmd)) {
              intent = new Intent(Constants.BROADCAST_INFRA_SYNC);
            } else if (Constants.PUSH_CONTROL_RESULT.equals(cmd)) {
              intent = new Intent(Constants.BROADCAST_CONTROL_RESULT);
            } else if (Constants.PUSH_NEW_INFRA_TYPE.equals(cmd)) {
              intent = new Intent(Constants.BROADCAST_INFRA_TYPE);
            } else if (Constants.PUSH_BIND_SUCCEED.equals(cmd)) {
              intent = new Intent(Constants.BROADCAST_BIND_SUCCEED);
            }
            if (intent != null) {
              intent.putExtra("content", jsonObject.toJSONString());
              manager.sendBroadcast(intent);
            }
          }
        }
      };
      JSONObject commonRequest = new JSONObject();
      commonRequest.put("sessionID", SharedPrefHelper.getInstance().getSessionID());
      String body = commonRequest.toJSONString();

      String url = CommonUtil.formatUrl(Constants.SUF_GET_PUSH_EVENT);
      request = new JsonObjectRequest(url, body, listener, null);
      request.setDescription("后台GET消息");
    }
    return request;
  }

  /*************************************
   * 从服务器获取系统消息 - END
   *************************************/

  /**
   * 重新获取sessionID
   */
  public void refreshSessionID() {
    //TODO 全局的唯一刷新sessionID的地方
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
