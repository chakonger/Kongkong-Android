package com.espressif.iot.esptouch;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.espressif.iot.esptouch.protocol.ConfigwlHelper;
import com.espressif.iot.esptouch.protocol.EsptouchGenerator;
import com.espressif.iot.esptouch.task.IEsptouchListener;
import com.espressif.iot.esptouch.task.IEsptouchResult;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot.esptouch.udp.UDPSocketClient;
import com.espressif.iot.esptouch.util.EspNetUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.leanvision.common.util.LogUtil;

public class __EsptouchTask implements __IEsptouchTask {

    /**
     * one indivisible data contain 3 9bits info
     */
    private static final int ONE_DATA_LEN = 3;

    private static final String TAG = "EsptouchTask";
    private byte[] mApssidByte;

    private volatile List<IEsptouchResult> mEsptouchResultList;
    private volatile boolean mIsSuc = false;
    private volatile boolean mIsInterrupt = false;
    private volatile boolean mIsExecuted = false;
    private final UDPSocketClient mSocketClient;
    private final String mApSsid;
    private final String mApBssid;
    private final boolean mIsSsidHidden;
    private final String mApPassword;
    private final Context mContext;
    private AtomicBoolean mIsCancelled;
    private EsptouchTaskParameter mParameter;
    private IEsptouchListener mEsptouchListener;

    public __EsptouchTask(String apSsid, String apBssid, String apPassword,
                          Context context, EsptouchTaskParameter parameter,
                          boolean isSsidHidden) {
        if (TextUtils.isEmpty(apSsid)) {
            throw new IllegalArgumentException(
                    "the apSsid should be null or empty");
        }
        if (apPassword == null) {
            apPassword = "";
        }
        mContext = context;
        mApSsid = apSsid;
        mApBssid = apBssid;
        mApPassword = apPassword;
        mIsCancelled = new AtomicBoolean(false);

        try {
            mApssidByte = mApSsid.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mSocketClient = new UDPSocketClient();
        mParameter = parameter;

        mIsSsidHidden = isSsidHidden;
        mEsptouchResultList = new ArrayList<>();
    }

    private List<IEsptouchResult> __getEsptouchResultList() {
        synchronized (mEsptouchResultList) {
            if (mEsptouchResultList.isEmpty()) {
                EsptouchResult esptouchResultFail = new EsptouchResult(false,
                        null, null);
                esptouchResultFail.setIsCancelled(mIsCancelled.get());
                mEsptouchResultList.add(esptouchResultFail);
            }

            return mEsptouchResultList;
        }
    }

    private synchronized void __interrupt() {
        if (!mIsInterrupt) {
            mIsInterrupt = true;
            mSocketClient.interrupt();
//            mSocketServer.interrupt();
            // interrupt the current Thread which is used to wait for udp response
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void interrupt() {
        if (__IEsptouchTask.DEBUG) {
            Log.d(TAG, "interrupt()");
        }
        mIsCancelled.set(true);
        __interrupt();
    }

    public void setIsSuc(boolean mIsSuc) {
        this.mIsSuc = mIsSuc;
    }

    private boolean __execute(EsptouchGenerator generator) throws IOException {
        long startTime = System.currentTimeMillis();
        long currentTime = startTime;
        long lastTime = currentTime - mParameter.getTimeoutTotalCodeMillisecond();

        byte[][] gcBytes2 = generator.getGCBytes2();
        byte[][] dcBytes2 = generator.getDCBytes2();
        int index = 0;

        String hostAddress = EspNetUtil.getLocalInetAddress(mContext).getHostAddress();
        byte[] configBytes;
        int k;
        int l;
        while (!mIsInterrupt) {
            long timeS = System.currentTimeMillis(); // 计时间用
            if (currentTime - lastTime >= mParameter.getTimeoutTotalCodeMillisecond()) { //2s
                LogUtil.log(getClass(), "----- send guide code -----");
                // send guide code - 2s
                int times = 0;
                LogUtil.log(getClass(), String.format("gcLength : %d", gcBytes2.length));
                while (!mIsInterrupt
                        && System.currentTimeMillis() - currentTime < mParameter
                        .getTimeoutGuideCodeMillisecond()) {
                    times++;
                    mSocketClient.sendData(gcBytes2,
                            mParameter.getTargetHostname(),
                            mParameter.getTargetPort(),
                            mParameter.getIntervalGuideCodeMillisecond());

                    // check whether the udp is send enough time
                    if (System.currentTimeMillis() - startTime > mParameter.getWaitUdpSendingMillisecond()) {
                        break;
                    }
                }
                lastTime = currentTime;

                long totalLength = 0;
                for (int i = 0; i < gcBytes2.length; i++) {
                    totalLength += gcBytes2[i].length;
                }
                LogUtil.log(getClass(), String.format("total bytes : %d", times * totalLength));
                continue;  // 这里发完引导CODE，就接着发dataCode
            } else {
                LogUtil.log(getClass(), "----- send data code -----");
                long dataCodeStartTime = System.currentTimeMillis();
                // send data code  - 4s
                while (!mIsInterrupt
                        && System.currentTimeMillis() - dataCodeStartTime < mParameter
                        .getTimeoutDataCodeMillisecond()) {
                    mSocketClient.sendData(dcBytes2, index, ONE_DATA_LEN,
                            mParameter.getTargetHostname(),
                            mParameter.getTargetPort(),
                            mParameter.getIntervalDataCodeMillisecond());
                    index = (index + ONE_DATA_LEN) % dcBytes2.length;
                }
            }
            LogUtil.log(getClass(), "1 . diff times : " + (System.currentTimeMillis() - timeS));
            timeS = System.currentTimeMillis();

            long totalLength = 0;
            //新岸线发送
            for (int m = 0; m < 3; m++) {
                configBytes = ConfigwlHelper.buildConfigPacket(mApssidByte, mApPassword, hostAddress);
                LogUtil.log(getClass(), "configPacket.length=" + configBytes.length);
                mSocketClient.sendDateForNewPort(configBytes);
            }
            LogUtil.log(getClass(), "2 . diff times : " + (System.currentTimeMillis() - timeS));
            LogUtil.log(getClass(), String.format("total bytes newport : %d", totalLength));

            currentTime = System.currentTimeMillis();
            // check whether the udp is send enough time
            if (currentTime - startTime > mParameter.getWaitUdpSendingMillisecond()) {
                break;
            }
        }
        LogUtil.log(getClass(), " Game Over ------ ");
        if (mEsptouchListener != null)
            mEsptouchListener.sendComplete(mIsSuc);
        return mIsSuc;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void __checkTaskValid() {
        // !!!NOTE: the esptouch task could be executed only once
        if (this.mIsExecuted) {
            throw new IllegalStateException(
                    "the Esptouch task could be executed only once");
        }
        this.mIsExecuted = true;
    }

    @Override
    public IEsptouchResult executeForResult() throws RuntimeException, IOException {
        return executeForResults(1).get(0);
    }

    @Override
    public boolean isCancelled() {
        return this.mIsCancelled.get();
    }

    @Override
    public List<IEsptouchResult> executeForResults(int expectTaskResultCount)
            throws RuntimeException, IOException {
        __checkTaskValid();

        mParameter.setExpectTaskResultCount(expectTaskResultCount);

        if (__IEsptouchTask.DEBUG) {
            Log.d(TAG, "execute()");
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException(
                    "Don't call the esptouch Task at Main(UI) thread directly.");
        }

        InetAddress localInetAddress = EspNetUtil.getLocalInetAddress(mContext);
        if (__IEsptouchTask.DEBUG) {
            Log.i(TAG, "localInetAddress: " + localInetAddress);
        }
        // generator the esptouch byte[][] to be transformed, which will cost
        // some time(maybe a bit much)
        EsptouchGenerator generator = new EsptouchGenerator(mApSsid, mApBssid,
                mApPassword, localInetAddress, mIsSsidHidden);

        // listen the esptouch result asyn
        boolean isSuc;
        for (int i = 0; i < mParameter.getTotalRepeatTime(); i++) {
            isSuc = __execute(generator);
            if (isSuc) {
                return __getEsptouchResultList();
            }
        }
        return __getEsptouchResultList();
    }

    @Override
    public void setEsptouchListener(IEsptouchListener esptouchListener) {
        mEsptouchListener = esptouchListener;
    }

}
