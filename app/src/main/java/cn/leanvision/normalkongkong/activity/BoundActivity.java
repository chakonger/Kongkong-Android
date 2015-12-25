package cn.leanvision.normalkongkong.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.mime.VolleyHelper;
import com.espressif.iot.esptouch.EspWifiAdminSimple;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.task.IEsptouchListener;
import com.espressif.iot.esptouch.task.IEsptouchResult;
import com.espressif.iot.esptouch.task.IEsptouchTask;
import com.espressif.iot.esptouch.udp.UDPSocketServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.CommonUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;
import cn.leanvision.normalkongkong.framework.LvIBaseHandler;
import cn.leanvision.normalkongkong.framework.activity.LvBaseActivity;
import cn.leanvision.normalkongkong.framework.request.JsonObjectRequest;
import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;
import cn.leanvision.normalkongkong.widget.BoundProgressView;
import cn.leanvision.normalkongkong.widget.RippleBackground;

/**
 * @author lvshicheng
 * @date 2015年12月22日12:10:05
 * @description 绑定页面 - 绑定过程中不要离开该页面
 */
public class BoundActivity extends LvBaseActivity {

    public static final int UDP_PORT = 7788;

    public static final int UDP_PAIR_RETURN = 27;

    public static final int BOUND_SUCCEED = 28;
    public static final int BOUND_FAILED = 30;

    public static final int BOUND_WAIT_DELAY = 29;

    @Bind(R.id.bottom_bpv)
    BoundProgressView mBoundProgressView;
    @Bind(R.id.tv_desc)
    TextView tv_desc;
    @Bind(R.id.content)
    RippleBackground rippleBackground;

    private String seed;
    private SharedPrefHelper sph;
    private String wifiSsid;
    private String wifiPwd;

    private ExecutorService espExecutorService;
    private EspTouchRunnable espTouchRunnable;

    private ExecutorService receiverExecutorService;
    private UdpReceiverRunnable udpReceiverRunnable;
    private LvHandler lvHandler;

    // 暂时这么处理
    private boolean isSeedSendSucceed = false;
    private BroadcastReceiver pushReceiver;

    public static Intent createIntent(Context context, String wifiSsid) {
        Intent intent = new Intent(context, BoundActivity.class);
        intent.putExtra("SSID", wifiSsid);
        return intent;
    }

    @Override
    protected void setContentViewLv() {
        /** 保持屏幕常亮 */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_bound);

        sph = SharedPrefHelper.getInstance();
        seed = CommonUtil.getDeviceRandomNum();

        wifiSsid = getIntent().getStringExtra("SSID");
        wifiPwd = sph.getWifiPwd();
        lvHandler = new LvHandler(this);
    }

    @Override
    protected void initViewLv() {
        setupToolbar(R.string.title_activity_bound);

        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();

        mBoundProgressView = (BoundProgressView) findViewById(R.id.bottom_bpv);
        mBoundProgressView.setStep(0);

        tv_desc = (TextView) findViewById(R.id.tv_desc);
        tv_desc.setText(R.string.desc_one);
    }

    @Override
    protected void afterInitView() {
        httpPostBound();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // seed 必须发送成功后才开始配对
        if (isSeedSendSucceed) {
            startUdpReceiver();
            startEsptouch();
        }
        registerPushReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopUdpReceiver();
        stopEsptouch(false);
        unregisterPushReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void registerPushReceiver() {
        if (pushReceiver == null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            pushReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (Constants.BROADCAST_BIND_SUCCEED.equals(action)) {
                        //TODO 绑定成功
                        mBoundProgressView.setStep(3);
                        lvHandler.sendEmptyMessageDelayed(BOUND_SUCCEED, 200);
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.BROADCAST_BIND_SUCCEED);
            manager.registerReceiver(pushReceiver, filter);
        }
    }

    public void unregisterPushReceiver() {
        if (pushReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.unregisterReceiver(pushReceiver);
            pushReceiver = null;
        }
    }

    /*******************************
     * http request zone - START
     *******************************/
    private void httpPostBound() {
        String url = CommonUtil.formatUrl(Constants.SUF_POST_BIND);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionID", sph.getSessionID());
        jsonObject.put("seed", seed);
        jsonObject.put("appid", Constants.APP_ID);
        String body = jsonObject.toJSONString();
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String errcode = response.getString("errcode");
                if (Constants.ERROR_CODE_SUCCEED.equals(errcode)) {
                    isSeedSendSucceed = true;
                    startBound();
                } else {
                    isSeedSendSucceed = false;
                    // SEED 发送失败
                    bindFailed();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(url, body, listener, errorListener);
        VolleyHelper.addRequest(this, request, requestTag);
    }

    /*******************************
     * http request zone - END
     *******************************/
    public void bindFailed() {
        lvHandler.sendEmptyMessage(BOUND_FAILED);
    }

    /**
     * 开始绑定
     */
    public void startBound() {
        startEsptouch();
        startUdpReceiver();
    }

    /**
     * 开始发送绑定信息
     */
    private void startEsptouch() {
        if (this.isFinishing())
            return;
        EspWifiAdminSimple mWifiAdmin = new EspWifiAdminSimple(this);
        String apBssid = mWifiAdmin.getWifiConnectedBssid();
        espExecutorService = Executors.newSingleThreadExecutor();
        //开始绑定，传入wifiSSID(wifi名称)，wifiPWD(wifi密码)，apBssid(路由器mac地址)
        espTouchRunnable = new EspTouchRunnable(wifiSsid, apBssid, wifiPwd, this);
        espExecutorService.execute(espTouchRunnable);
    }

    /**
     * 停止发送绑定信息
     */
    private void stopEsptouch(boolean b) {
        if (espTouchRunnable != null)
            espTouchRunnable.cancel(b);
        if (espExecutorService != null) {
            espExecutorService.shutdown();
            espExecutorService = null;
        }
    }

    /**
     * 开始监听插座返回信息
     */
    public void startUdpReceiver() {
        if (null == receiverExecutorService) {
            receiverExecutorService = Executors.newSingleThreadExecutor();
            udpReceiverRunnable = new UdpReceiverRunnable();
            receiverExecutorService.execute(udpReceiverRunnable);
        }
    }

    /**
     * 停止监听插座返回信息
     */
    public void stopUdpReceiver() {
        if (null != udpReceiverRunnable) {
            udpReceiverRunnable.cancel();
        }
        if (null != receiverExecutorService) {
            receiverExecutorService.shutdown();
            receiverExecutorService = null;
        }
    }

    /**
     * 绑定发送结束返回
     */
    private IEsptouchListener myListener = new IEsptouchListener() {
        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
        }

        @Override
        public void sendComplete(boolean b) {
            LogUtil.log(getClass(), "send Complete");
            if (b) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //END SEND
                        mBoundProgressView.setStep(1);
                    }
                });
                //等待20s
                lvHandler.sendEmptyMessageDelayed(BOUND_WAIT_DELAY, 20 * 1000);
            } else {
                bindFailed();
            }
        }
    };

    /**
     * 执行绑定任务
     */
    public class EspTouchRunnable implements Runnable {

        private final String apSsid;
        private final String apBssid;
        private final String apPassword;
        private IEsptouchTask mEsptouchTask;
        private WeakReference<BoundActivity> wActivity;

        public EspTouchRunnable(String apSsid, String apBssid, String apPassword, BoundActivity mActvity) {
            this.apSsid = apSsid;
            this.apBssid = apBssid;
            this.apPassword = apPassword;
            wActivity = new WeakReference<>(mActvity);
        }

        @Override
        public void run() {
            BoundActivity activity = wActivity.get();
            if (activity == null)
                return;
            boolean isSsidHidden = false;
            mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                    isSsidHidden, activity);
            mEsptouchTask.setEsptouchListener(activity.myListener);
            try {
                mEsptouchTask.executeForResults(1);
            } catch (IOException e) {
                //TODO 报文发送失败？？？
                LogUtil.log(getClass(), " ------ 发送报文错误！！！");
                e.printStackTrace();
                //结束得了
                cancel(false);
            }
        }

        public void cancel(boolean b) {
            if (mEsptouchTask != null) {
                mEsptouchTask.setIsSucceed(b);
                mEsptouchTask.interrupt();
            }
        }
    }

    /**
     * 监听绑定过程中插座连接路由器后返回的报文
     */
    public class UdpReceiverRunnable implements Runnable {

        private UDPSocketServer lvUdpServer;
        private int count = 0;

        public UdpReceiverRunnable() {

        }

        @Override
        public void run() {
            lvUdpServer = new UDPSocketServer(UDP_PORT, -1, getApplicationContext());
            String result = lvUdpServer.receiveSpecLenBytes();

            if (StringUtil.isNullOrEmpty(result)) {
                //do nothing
            } else {
                //返回结果
                LogUtil.log(BoundActivity.this.getClass(), "RESULT返回数据 : " + result);
                JSONObject parseObject = JSONObject.parseObject(result);
                String devSn = parseObject.getString("devSn");

                String address = parseObject.getString("address");
                int port = parseObject.getIntValue("port");

                // 这里我拿到了连接WIFI后的返回信息进行服务器配置
                // 将服务器信息通过UDP发送给插座
                String serverAddress = Constants.BIND_ADDRESS;
                String serverAddressPort = Constants.BIND_PORT;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("domain", serverAddress);
                jsonObject.put("port", serverAddressPort);
                jsonObject.put("seed", seed);
                jsonObject.put("devSn", devSn);
                /**
                 * {"domain":"域名","port":"端口","seed":"5位随机数(当devSn已经是20位时可发可不发)"
                 * , "devSn":"设备号(应该与以前插座发出的完全一致)"}
                 * */
                LogUtil.e("发送数据 ---" + jsonObject.toString());
                sendServerInfo(address, port, jsonObject);
                // 停止发送绑定信息
                Message message = lvHandler.obtainMessage(BoundActivity.UDP_PAIR_RETURN);
                message.obj = devSn;
                lvHandler.sendMessage(message);
            }
        }

        /**
         * 通过UDP发送服务器信息到插座
         */
        private void sendServerInfo(String address, int port, JSONObject jsonObject) {
            try {
                DatagramSocket datagramSocket = new DatagramSocket();
                byte[] data = jsonObject.toString().getBytes("UTF-8");
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(address), port);
                datagramSocket.send(datagramPacket);
                Thread.sleep(200);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                // 只处理IO异常
                if (count < 3) {
                    sendServerInfo(address, port, jsonObject);
                } else {
                    //TODO 绑定失败
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            if (lvUdpServer != null) {
                lvUdpServer.interrupt();
                lvUdpServer = null;
            }
        }
    }

    public static class LvHandler extends LvIBaseHandler<BoundActivity> {

        public LvHandler(BoundActivity boundActivity) {
            super(boundActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!canGoNext())
                return;
            BoundActivity activity = getActivity();
            switch (msg.what) {
                case UDP_PAIR_RETURN:
                    //TODO 等待插座上线  N6A0
                    activity.stopEsptouch(true);
                    break;
                case BOUND_SUCCEED:
                    this.removeMessages(BOUND_WAIT_DELAY);
                    Intent intent = MainActivity.createIntent(activity, MainActivity.TYPE_BOUNDED);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                    break;
                case BOUND_WAIT_DELAY:
                case BOUND_FAILED:
                    activity.showSnackBar(R.string.bound_failed);
                    activity.finish();
                    break;
            }
        }
    }
}
