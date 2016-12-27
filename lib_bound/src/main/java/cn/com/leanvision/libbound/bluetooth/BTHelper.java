package cn.com.leanvision.libbound.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import cn.com.leanvision.libbound.bluetooth.utils.BluetoothService;
import cn.com.leanvision.libbound.bluetooth.utils.BluetoothState;

/********************************
 * Created by lvshicheng on 2016/10/28.
 ********************************/
public class BTHelper {

    private String TAG = BTHelper.class.getSimpleName();

    /**
     * 蓝牙重连次数
     */
    private static final int RETRY_CONNECT_COUNT   = 2;
    /**
     * 蓝牙搜索次数
     */
    private static final int RETRY_DISCOVERY_COUNT = 3;
    private static final int PAIR_TASK_RUNNING     = 5;
    private static final int PAIR_TASK_STOP        = 6;
    private static final int RETRY_READ_COUNT      = 25;

    private int     pairTaskStatue   = PAIR_TASK_STOP;
    private int     nDiscoveryCount  = 0;
    private int     nConnCount       = 0;
    private int     nReadCount       = 0;
    private boolean hasGetDeviceInfo = false;

    private final Context           context;
    private final BTCallback        callback;
    private final String            seed;
    private       BluetoothAdapter  mBluetoothAdapter;
    private       BroadcastReceiver receiver;
    private       BluetoothService  mChatService;
    private       BluetoothDevice   device;
    private       ChatHandler       mHandler;

    /**
     * 设备IMEI号
     */
    private String imei;
    private String ipAddress;

    // IPAddrGet@seed:12345 随机码范围01001->65534
    private String[] cmds =
            {"WiFiEntrySet@ssid:%s@pin:%s@svrIP:%s@svrPort:%s", "IPAddrGet", "DeviceIDGet"};

    public BTHelper(Context context, BTCallback callback, String seed) {
        this.context = context;
        this.callback = callback;
        this.seed = seed;

        mHandler = new ChatHandler();
    }

    public BTHelper makeCmd(String ssid, String pwd, String address, String port) {
        cmds[0] = String.format(cmds[0], ssid, pwd, address, port);
        return this;
    }

    public BTHelper initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            bttError(ErrorCode.BT_UNAVAILABLE);
            return null;
        }

        // 未开启了蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            callback.btError(ErrorCode.BT_CLOSED);
        } else {
            startBluetooth();
        }
        return this;
    }

    public void stopBluetooth() {
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.cancelDiscovery(); // 停止搜索
        }
        // 清空消息队列
        if (mHandler != null) {
            mHandler.removeMessages(BluetoothState.MESSAGE_STATE_CHANGE);
            mHandler.removeMessages(BluetoothState.MESSAGE_READ);
            mHandler = null;
        }
        // 取消广播注册
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
        if (mChatService != null) // 停止通讯
            mChatService.stop();
    }

    private void startBluetooth() {
        registerBluetoothBroadcastReceiver();
        startDiscovery();
    }

    private void registerBluetoothBroadcastReceiver() {
        if (receiver != null)
            return;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) { // 开始搜索
                    Log.e(TAG, "ACTION_DISCOVERY_STARTED");
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { // 搜索结束
                    Log.e(TAG, "ACTION_DISCOVERY_FINISHED");
                    if (mChatService != null && mChatService.getState() == BluetoothService.STATE_CONNECTING)// 如果正在连接就算了
                        return;
                    if (PAIR_TASK_RUNNING == pairTaskStatue) // 正在配对就不处理了
                        return;
                    if ((device == null || device.getName() == null || !device.getName().startsWith("CHAKONGER")) && nDiscoveryCount <= RETRY_DISCOVERY_COUNT) { // 重新搜索
                        startDiscovery();
                    } else if (device == null) {

                    } else {
                        connectDevice(device);
                    }
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    /* 从intent中取得搜索结果数据 */
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.e(TAG, "device : " + device.getName() + " : " + device.toString() + " : " + device.getBondState());
                    // device : CHAKONGER-0225505556 : 41:02:25:50:55:56 : 10
                    if (device != null && device.getName() != null) {
                        // CHAKONGER-0225506406
                        if (device.getName().startsWith("CHAKONGER")) {
                            if (mBluetoothAdapter == null)
                                return;
                            if (mBluetoothAdapter.isDiscovering())
                                mBluetoothAdapter.cancelDiscovery(); // 停止搜索
                        } else {
                            device = null;
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
    }

    private void startDiscovery() {
        if (nDiscoveryCount >= RETRY_DISCOVERY_COUNT) {
            bttError(ErrorCode.BT_OVER);
            return;
        }
        device = null;
        nConnCount = 0;
        nDiscoveryCount++;

        if (mBluetoothAdapter != null && !mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
        }
    }

    private void connectDevice(BluetoothDevice device) {
        if (mChatService == null)
            mChatService = new BluetoothService(context, mHandler);
        mChatService.connect(device, false);
    }

    private void startWrite() {
        bttStartChat();
        sendMessage(cmds[0]);
    }

    private void sendMessage(final String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() == BluetoothState.STATE_CONNECTING) {
            return;
        } else if (mChatService.getState() != BluetoothState.STATE_CONNECTED) {
            if (device == null) {
                Log.e(TAG, "请选择要连接的蓝牙设备");
                return;
            }
            mChatService.connect(device, false);
            return;
        }

        // Check that there's actually something to send
        if (!TextUtils.isEmpty(message)) {
            Log.e(TAG, "sendmsg = " + message);
            // Get the message bytes and tell the BluetoothChatService to write
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    byte[] send = message.getBytes();
                    mChatService.write(send);
                }
            }, 2000);
        }
    }

    private void handleReadData(String read) {
        nReadCount++;
        if (TextUtils.isEmpty(read))
            return;

        if (read.startsWith("WiFiEntrySet")) {
            // WiFiEntrySet@OK
            if (!read.contains("OK")) {
                if (nReadCount >= RETRY_READ_COUNT) {
                    bttError(ErrorCode.BT_OVER);
                } else {
                    startWrite();
                }
            } else {
                sendMessage(cmds[2] + "@seed:" + seed);
            }
            return;
        } else if (read.startsWith("IPAddrGet")) {
            // IPAddrGet@ip:255.255.255.255@hotSpotMac:00-00-00-00-00-00-00
            String[] data = read.split("@");
            if (data.length == 3) {
                ipAddress = data[1].trim().substring(3, data[1].length());
            }

            if (TextUtils.isEmpty(ipAddress) || "255.255.255.255".equals(ipAddress)) {
                if (nReadCount == RETRY_READ_COUNT) {
                    bttError(ErrorCode.BT_OVER);
                } else {
                    sendMessage(cmds[1]);
                }
                return;
            }
        } else if (read.startsWith("DeviceIDGet")) {
            // DeviceIDGet@mac:00-e0-4c-95-29-29@imei:311410225505309
            String[] data = read.split("@");
            if (data.length == 3) {
                imei = data[2].split(":")[1];
            }

            if (TextUtils.isEmpty(imei)) {
                if (nReadCount == RETRY_READ_COUNT) {
                    bttError(ErrorCode.BT_OVER);
                } else {
                    sendMessage(cmds[2] + "@seed:" + seed);
                }
            } else {
                // 获取IP
                sendMessage(cmds[1]);
            }
            return;
        }

        // 最后校验需要的数据信息,开始注册设备信息
        if (!TextUtils.isEmpty(imei) && !TextUtils.isEmpty(ipAddress) && !"255.255.255.255".equals(ipAddress)) {
            hasGetDeviceInfo = true;
            bttProgress(2);
            // 测试蓝牙绑定推送消息
            bttSuccess();
        } else {
            bttError(ErrorCode.BT_OVER);
        }
    }

    private void bttError(ErrorCode errorCode) {
        if (callback != null)
            callback.btError(errorCode);
    }

    // TODO 这里不确定要回传哪些参数
    private void bttSuccess() {
        if (callback != null)
            callback.btSuccess();
    }

    private void bttStartChat() {
        if (callback != null)
            callback.btStartChat();
    }

    private void bttProgress(int step) {
        if (callback != null)
            callback.btProgress(step);
    }

    public class ChatHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothState.MESSAGE_STATE_CHANGE: // 状态改变
                    switch (msg.arg1) {
                        case BluetoothState.STATE_CONNECTED: // 连接成功
                            startWrite();
                            bttProgress(1);
                            break;
                        case BluetoothState.STATE_CONNECTING: // 连接中
                            break;
                        case BluetoothState.STATE_LISTEN: // 连接失败
                            if (hasGetDeviceInfo)
                                return;
                            // 连接失败就重新连接
                            if (nConnCount < RETRY_CONNECT_COUNT) {
                                nConnCount++;
                                connectDevice(device);
                            } else {
                                bttError(ErrorCode.BT_OVER);
                            }
                            break;
                        case BluetoothState.STATE_NONE: // 连接失败，重试两次，这个就需要插拔插控，这里重新搜索意义不大
                            break;
                    }
                    break;
                case BluetoothState.MESSAGE_READ: // 通过蓝牙读取的数据
                    /** 获取返回数据 */
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage;
                    try {
                        // 65536
                        if (msg.arg1 > 1024)
                            return;
                        readMessage = new String(readBuf, 0, msg.arg1, "UTF-8");
                        Log.e(TAG, "read = " + readMessage);
                        handleReadData(readMessage.trim());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
