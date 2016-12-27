package com.nufront.nusmartconfig;

import android.util.Log;

import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot.esptouch.udp.UDPSocketClient;

import cn.com.leanvision.libbound.rx.RxBus;
import cn.com.leanvision.libbound.rx.busEvent.MsgEvent;

/********************************
 * Created by lvshicheng on 16/9/27.
 ********************************/
public class ConfigWireless implements __IEsptouchTask {

  private static final String TAG = "ConfigWireless";

  public  byte[]          ssid;
  public  String          pwd;
  public  String          ipAddr;
  public  byte[]          configPacket;
  private UDPSocketClient mSocketClient;

  private volatile boolean mIsInterrupt = false;
  private          int     debugCount   = -1;

  public ConfigWireless(String ssid, String pwd, String ipAddr) {
    this.ssid = ssid.getBytes();
    this.pwd = pwd;
    this.ipAddr = ipAddr;

    mSocketClient = new UDPSocketClient();
  }

  @Override
  public void execute() {
    long startTime = System.currentTimeMillis();
    int l;
    int k;
    configPacket = ConfigwlHelper.buildConfigPacket(ssid, pwd, ipAddr);
    Log.e(ConfigWireless.class.getSimpleName(), String.format("发送数据 : %s - %s - %s", new String(ssid), pwd, ipAddr));
    byte[] srcbs = new byte[1024];
    System.arraycopy(configPacket, 0, srcbs, 0, configPacket.length);
    k = 1;
    for (int i = 0; !mIsInterrupt && i < configPacket.length; i++) {
      l = (configPacket[i] + 256) % 256;
      l = l == 0 ? 129 : l;
      l = k == 1 ? (156 + l) : (156 - l);
      k = 1 - k;
      byte[] bs = new byte[l];
      System.arraycopy(srcbs, 0, bs, 0, l);
      if (debugCount < i) {
        debugCount = i;
        if (debugCount == 0)
          RxBus.getInstance().postEvent(new MsgEvent("新岸线发送报文："));
        Log.e(TAG, "data[" + i + " +].length = " + bs.length);
        RxBus.getInstance().postEvent(new MsgEvent("data[" + i + " +].length = " + bs.length));
      }
      String temp = "新岸线每个报文间隔3ms发送20次，然后等待30ms发送下一个报文\r\n发送UDP目标IP： 239.1.2.110:60001" + "\r\n" + "data[" + i + " +].length = " + bs.length;
      MsgEvent msgEvent = new MsgEvent(temp);
      msgEvent.type = "real_time";
      RxBus.getInstance().postEvent(msgEvent);
      // 这里发送20次,每次间隔3ms
      mSocketClient.sendData(bs, 20, "239.1.2.110", 60001, 3);
      try {
        Thread.sleep(30); // wait 30 ms
      } catch (InterruptedException e) {
        e.printStackTrace();
        mIsInterrupt = true;
      }
    }
    long totalUsedTime = System.currentTimeMillis() - startTime;
    Log.d(TAG, "一次用时 : " + totalUsedTime);
  }

  @Override
  public void interrupt() {
    if (__IEsptouchTask.DEBUG) {
      Log.d(TAG, "interrupt()");
    }
    __interrupt();
  }

  private synchronized void __interrupt() {
    if (!mIsInterrupt) {
      mIsInterrupt = true;
      mSocketClient.interrupt();
      // interrupt the current Thread which is used to wait for udp response
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public boolean isCancelled() {
    return false;
  }
}
