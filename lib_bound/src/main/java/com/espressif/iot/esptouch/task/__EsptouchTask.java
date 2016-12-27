package com.espressif.iot.esptouch.task;

import android.content.Context;
import android.util.Log;

import com.espressif.iot.esptouch.protocol.EsptouchGenerator;
import com.espressif.iot.esptouch.udp.UDPSocketClient;
import com.espressif.iot.esptouch.util.EspNetUtil;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.com.leanvision.libbound.rx.RxBus;
import cn.com.leanvision.libbound.rx.busEvent.MsgEvent;

import static com.espressif.iot.esptouch.util.CheckUtils.checkNotNull;

/********************************
 * Created by lvshicheng on 16/9/19.
 ********************************/
public class __EsptouchTask implements __IEsptouchTask {

  private static final String TAG = "EsptouchTask";

  /**
   * one indivisible data contain 3 9bits info
   */
  private static final int ONE_DATA_LEN = 3;

  private final UDPSocketClient        mSocketClient;
  //    private final UDPSocketServer        mSocketServer;
  private final String                 mApSsid;
  private final String                 mApBssid;
  private final boolean                mIsSsidHidden;
  private final String                 mApPassword;
  private final Context                mContext;
  private       IEsptouchTaskParameter mParameter;
  private       AtomicBoolean          mIsCancelled;

  private volatile boolean mIsInterrupt = false;

  public __EsptouchTask(String apSsid,
                        String apBssid,
                        String apPassword,
                        Context context,
                        IEsptouchTaskParameter parameter,
                        boolean isSsidHidden) {

    checkNotNull(apBssid, "the apSsid should not be null or empty");

    if (apPassword == null)
      apPassword = "";

    mContext = context;
    mApSsid = apSsid;
    mApBssid = apBssid;
    mApPassword = apPassword;
    mIsSsidHidden = isSsidHidden;
    mParameter = parameter;

    InetAddress localInetAddress = EspNetUtil.getLocalInetAddress(mContext);
    if (__IEsptouchTask.DEBUG) {
      Log.i(TAG, String.format("ssid : %s  pwd : %s ", mApSsid, mApPassword));
    }

    mSocketClient = new UDPSocketClient();
//        mSocketServer = new UDPSocketServer(mParameter.getPortListening(),
//                mParameter.getWaitUdpTotalMillisecond(), context);
    mIsCancelled = new AtomicBoolean(false);
  }

  @Override
  public void execute() {

    long startTime = System.currentTimeMillis();
    long currentTime = startTime;
    long lastTime = currentTime - mParameter.getTimeoutTotalCodeMillisecond();

    InetAddress localInetAddress = EspNetUtil.getLocalInetAddress(mContext);
    if (__IEsptouchTask.DEBUG) {
      Log.i(TAG, "localInetAddress: " + localInetAddress);
    }
    IEsptouchGenerator generator = new EsptouchGenerator(mApSsid, mApBssid,
        mApPassword, localInetAddress, mIsSsidHidden);
    byte[][] gcBytes2 = generator.getGCBytes2();
    byte[][] dcBytes2 = generator.getDCBytes2();

    RxBus.getInstance().postEvent(new MsgEvent("乐鑫发送报文："));
    for (int i = 0; i < gcBytes2.length; i++) {
      RxBus.getInstance().postEvent(new MsgEvent( "gcBytes[" + i + " +].length = " + gcBytes2[i].length));
    }
    RxBus.getInstance().postEvent(new MsgEvent("\r\n"));
    for (int i = 0; i < dcBytes2.length; i++) {
      RxBus.getInstance().postEvent(new MsgEvent( "dcBytes[" + i + " +].length = " + dcBytes2[i].length));
    }
    int index = 0;
    while (!mIsInterrupt) {
      if (currentTime - lastTime >= mParameter.getTimeoutTotalCodeMillisecond()) {
        if (__IEsptouchTask.DEBUG)
          Log.e(TAG, "send gc code ");

        // 发送2秒钟的Guide ： [515,514,513,512]
        while (!mIsInterrupt
            && System.currentTimeMillis() - currentTime < mParameter.getTimeoutGuideCodeMillisecond()) {
          mSocketClient.sendData(gcBytes2,
              mParameter.getTargetHostname(),
              mParameter.getTargetPort(),
              mParameter.getIntervalGuideCodeMillisecond());
          // check whether the udp is send enough time.
          if (System.currentTimeMillis() - startTime > mParameter.getWaitUdpSendingMillisecond())
            break;
        }
        lastTime = currentTime;
      } else {
        if (__IEsptouchTask.DEBUG)
          Log.e(TAG, "send gc data ");
        // 发送4秒钟的Data
        mSocketClient.sendData(dcBytes2, index, ONE_DATA_LEN,
            mParameter.getTargetHostname(),
            mParameter.getTargetPort(),
            mParameter.getIntervalDataCodeMillisecond());
        index = (index + ONE_DATA_LEN) % dcBytes2.length;
      }

      currentTime = System.currentTimeMillis();
      // check whether the udp is send enough time
      if (currentTime - startTime > mParameter.getWaitUdpSendingMillisecond()) {
        // 时间到 - 默认45s
        break;
      }
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
  public boolean isCancelled() {
    return this.mIsCancelled.get();
  }
}
