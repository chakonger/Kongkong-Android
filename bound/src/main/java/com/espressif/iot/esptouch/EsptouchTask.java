package com.espressif.iot.esptouch;

import android.content.Context;

import com.espressif.iot.esptouch.task.IEsptouchListener;
import com.espressif.iot.esptouch.task.IEsptouchResult;
import com.espressif.iot.esptouch.task.IEsptouchTask;

import java.io.IOException;
import java.util.List;

public class EsptouchTask implements IEsptouchTask {

    public __EsptouchTask _mEsptouchTask;
    private EsptouchTaskParameter _mParameter;

    /**
     * Constructor of EsptouchTask
     *
     * @param apSsid       the Ap's ssid
     * @param apBssid      the Ap's bssid
     * @param apPassword   the Ap's password
     * @param isSsidHidden whether the Ap's ssid is hidden
     * @param context      the Context of the Application
     */
    public EsptouchTask(String apSsid, String apBssid, String apPassword,
                        boolean isSsidHidden, Context context) {
        _mParameter = new EsptouchTaskParameter();
        _mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword,
                context, _mParameter, isSsidHidden);
    }

    @Override
    public void interrupt() {
        _mEsptouchTask.interrupt();
    }

    @Override
    public IEsptouchResult executeForResult() throws RuntimeException, IOException {
        return _mEsptouchTask.executeForResult();
    }

    @Override
    public boolean isCancelled() {
        return _mEsptouchTask.isCancelled();
    }

    @Override
    public List<IEsptouchResult> executeForResults(int expectTaskResultCount)
            throws RuntimeException, IOException {
        if (expectTaskResultCount <= 0) {
            expectTaskResultCount = Integer.MAX_VALUE;
        }
        return _mEsptouchTask.executeForResults(expectTaskResultCount);
    }

    @Override
    public void setEsptouchListener(IEsptouchListener esptouchListener) {
        _mEsptouchTask.setEsptouchListener(esptouchListener);
    }

    @Override
    public void setIsSucceed(boolean b) {
        _mEsptouchTask.setIsSuc(b);
    }
}
