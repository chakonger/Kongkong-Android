package com.espressif.iot.esptouch.udp;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UDPSocketServer {

    private static final String TAG = "UDPSocketServer";
    public static final int LENGTH = 1024;
    private DatagramPacket mReceivePacket;
    private DatagramSocket mServerSocket;
    private Context mContext;
    private WifiManager.MulticastLock mLock;
    private final byte[] buffer;
    private volatile boolean mIsClosed;

    private synchronized void acquireLock() {
        if (mLock != null && !mLock.isHeld()) {
            mLock.acquire();
        }
    }

    private synchronized void releaseLock() {
        if (mLock != null && mLock.isHeld()) {
            try {
                mLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        }
    }

    /**
     * Constructor of UDP Socket Server
     *
     * @param port          the Socket Server port
     * @param socketTimeout the socket read timeout
     * @param context       the context of the Application
     */
    public UDPSocketServer(int port, int socketTimeout, Context context) {
        this.mContext = context;
        this.buffer = new byte[LENGTH];
        this.mReceivePacket = new DatagramPacket(buffer, LENGTH);
        try {
            this.mServerSocket = new DatagramSocket(port);
            if (socketTimeout > 0)
                this.mServerSocket.setSoTimeout(socketTimeout);
            this.mIsClosed = false;

            WifiManager manager = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            mLock = manager.createMulticastLock("udpLock");
            Log.d(TAG, "mServerSocket is created, socket read timeout: "
                    + socketTimeout + ", port: " + port);
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            e.printStackTrace();
        }
    }

    /**
     * Set the socket timeout in milliseconds
     *
     * @param timeout the timeout in milliseconds or 0 for no timeout.
     * @return true whether the timeout is set suc
     */
    public boolean setSoTimeout(int timeout) {
        try {
            this.mServerSocket.setSoTimeout(timeout);
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 封装返回的数据
     */
    public String receiveSpecLenBytes() {
        try {
            acquireLock();
            mServerSocket.receive(mReceivePacket);
            byte[] recData = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());

            String address = mReceivePacket.getAddress().getHostAddress();
            int port = mReceivePacket.getPort();

            String result = new String(recData);
            JSONObject jsonObject = JSONObject.parseObject(result);
            jsonObject.put("address", address);
            jsonObject.put("port", port);
            return jsonObject.toJSONString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void interrupt() {
        Log.i(TAG, "USPSocketServer is interrupt");
        close();
    }

    private synchronized void close() {
        if (!this.mIsClosed) {
            Log.e(TAG, "mServerSocket is closed");
            mServerSocket.close();
            releaseLock();
            this.mIsClosed = true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
