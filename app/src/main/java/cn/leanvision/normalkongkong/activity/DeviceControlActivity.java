package cn.leanvision.normalkongkong.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.mime.VolleyHelper;

import butterknife.Bind;
import cn.leanvision.common.bean.ChatMsgEntity;
import cn.leanvision.common.bean.DeviceInfo;
import cn.leanvision.common.util.CmdAction;
import cn.leanvision.common.util.DeviceTypeUtil;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.common.util.NetUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.CommonUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;
import cn.leanvision.normalkongkong.adapter.ChatDeviceRAdapter;
import cn.leanvision.normalkongkong.framework.LvIBaseHandler;
import cn.leanvision.normalkongkong.framework.activity.LvBaseActivity;
import cn.leanvision.normalkongkong.framework.request.JsonObjectRequest;
import cn.leanvision.normalkongkong.framework.response.parser.BaseParser;
import cn.leanvision.normalkongkong.widget.ControllerPanelView;

/**
 * @author lvshicheng
 * @date 2015年12月23日10:05:39
 * @description 设备控制窗口
 */
public class DeviceControlActivity extends LvBaseActivity {

    /**
     * 改消息用于等待单次操作是否成功
     */
    private static final int CHECK_SEND_STATUS = 0x2021;

    private static final int TURN_TO_END = 0x2022;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.root_controller_panel)
    ControllerPanelView mControllerPanelView;
    private DeviceInfo mDeviceInfo;

    private boolean cmdIsSending;
    private ChatDeviceRAdapter msgRAdapter;

    private String showResult;
    private MHandler mInterfaceHandler;
    private BroadcastReceiver pushReceiver;

    public static Intent createIntent(Context context, DeviceInfo mDeviceInfo) {
        Intent intent = new Intent(context, DeviceControlActivity.class);
        intent.putExtra("device", mDeviceInfo);
        return intent;
    }

    @Override
    protected void setContentViewLv() {
        setContentView(R.layout.activity_device_control);

        mDeviceInfo = getIntent().getParcelableExtra("device");
        mInterfaceHandler = new MHandler(this);
    }

    @Override
    protected void initViewLv() {
        setupToolbar(mDeviceInfo.getDevName(), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        msgRAdapter = new ChatDeviceRAdapter(this);
        recyclerView.setAdapter(msgRAdapter);
    }

    @Override
    protected void afterInitView() {
        mControllerPanelView.setDeviceInfo(mDeviceInfo);
        registerPushReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_remove) {
            httpGetRemoveDevice();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPushReceiver();
    }

    /**
     * 把字符串显示到屏幕上面
     */
    public void displayMimeText(String str) {
        if (!NetUtil.isNetWorkAvailable(getApplicationContext()))
            return;
        if (StringUtil.isNotNull(str) && !cmdIsSending) { // 如果正在发送也不允许发送消息
            // 改变界面操作
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setDate(CommonUtil.getDate());
            entity.setComMsg(false);
            entity.setText(str);
            entity.setSendCompleted(true);
            msgRAdapter.getDataList().add(entity);
            showItemProgressbar(true);
            showItemWarnIcon(true);
            mInterfaceHandler.sendEmptyMessage(TURN_TO_END);
        }
    }

    private void showItemWarnIcon(boolean bool) {
        if (msgRAdapter.getItemCount() == 0) {
            return;
        }
        int index = msgRAdapter.getItemCount() - 1;
        msgRAdapter.getDataList().get(index).setSendCompleted(bool);
    }

    private void showItemProgressbar(boolean bool) {
        if (msgRAdapter.getItemCount() == 0) {
            return;
        }
        int index = msgRAdapter.getItemCount() - 1;
        msgRAdapter.getDataList().get(index).setShowProgressbar(bool);
    }

    private void parseResult(String result) {
        //TODO 这里有个问题，涉及到多个设备间切换导致的命令同步问题？ --- 貌似只需要做开关和空调的同步
        //infraTypeID
        try {
            JSONObject jb = JSONObject.parseObject(result);
            String devStatus = jb.getString("devStatus");
            //TODO 返回信息修改一次设备状态
            if(!devStatus.equals(msgRAdapter.getDevStatus())){
                msgRAdapter.setDevStatus(devStatus);
            }
            //设备类型问题
            String rtn = jb.getString("errcode");
            if (Constants.ERROR_CODE_SUCCEED.equals(rtn)) {
                String content = jb.getString("errmsg");
                displayDeviceText(content, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设备的消息 - 需要记住当时的设备类型和状态
     */
    private void displayDeviceText(String msg, String url) {
        final ChatMsgEntity entity = new ChatMsgEntity();
        entity.setDate(CommonUtil.getDate());
        entity.setComMsg(true);
        entity.setText(msg);
        entity.setSendCompleted(true);
        entity.setNativePath(url);
        msgRAdapter.getDataList().add(entity);
        mInterfaceHandler.sendEmptyMessage(TURN_TO_END);
    }

    public void registerPushReceiver() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        pushReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String content = intent.getStringExtra("content");
                JSONObject jsonObject = JSONObject.parseObject(content);
                if (Constants.BROADCAST_STATUS.equals(action)) {
                    //变更状态
                    String devStatus = jsonObject.getString("devStatus");
                    if (msgRAdapter != null && !msgRAdapter.getDevStatus().equals(devStatus)) {
                        msgRAdapter.setDevStatus(devStatus);
                        msgRAdapter.notifyDataSetChanged();
                    }
                } else if (Constants.BROADCAST_INFRA_SYNC.equals(action)) {
                    //同步控制面板状态
                    String infraInst = jsonObject.getString("infraInst");
                    String bigType = jsonObject.getString("bigType");
                    String infraTypeID = jsonObject.getString("infraTypeID");
                    mControllerPanelView.setLastInst(CmdAction.CMD_ACTION_6, infraInst, bigType, infraTypeID);
                } else if (Constants.BROADCAST_CONTROL_RESULT.equals(action)) {
                    //控制成功返回
                    String infraInst = jsonObject.getString("inst");
                    String bigType = jsonObject.getString("bigType");
                    String infraTypeID = jsonObject.getString("infraTypeID");
                    String actionID = jsonObject.getString("actionID");

                    // 这里返回同时也意味着操作成功
                    if (showResult != null) {
                        showItemProgressbar(false);
                        parseResult(showResult);
                        showResult = null;
                        cmdIsSending = false;
                        mInterfaceHandler.removeMessages(CHECK_SEND_STATUS);
                    }
                    mControllerPanelView.setLastInst(actionID, infraInst, bigType, infraTypeID);
                } else if (Constants.BROADCAST_INFRA_TYPE.equals(action)) {
                    //TODO 新识别红外  - 查询一次单个设备信息
                    httpPostDeviceQuery();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_STATUS);
        filter.addAction(Constants.BROADCAST_INFRA_SYNC);
        filter.addAction(Constants.BROADCAST_CONTROL_RESULT);
        filter.addAction(Constants.BROADCAST_INFRA_TYPE);
        manager.registerReceiver(pushReceiver, filter);
    }

    public void unregisterPushReceiver() {
        if (pushReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.unregisterReceiver(pushReceiver);
            pushReceiver = null;
        }
    }

    static class MHandler extends LvIBaseHandler<DeviceControlActivity> {

        public MHandler(DeviceControlActivity chatDeviceNewActivity) {
            super(chatDeviceNewActivity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            if (!canGoNext())
                return;
            DeviceControlActivity activity = getActivity();
            switch (msg.what) {
                case CHECK_SEND_STATUS:
                    // 如果到现在还未返回则说明超时,判定执行失败
                    activity.showResult = null;
                    activity.showItemProgressbar(false);
                    activity.showItemWarnIcon(false);
                    //FIXME 超时就没有消息回来, 在result里面找infraTypeID
                    activity.displayDeviceText(activity.getString(R.string._n_time_out), null);
                    activity.cmdIsSending = false;
                    break;
                case TURN_TO_END:
                    LogUtil.log(getClass(), "count : " + activity.msgRAdapter.getItemCount());
                    activity.msgRAdapter.notifyDataSetChanged();
                    int count = activity.msgRAdapter.getItemCount();
                    if (count > 1) {
                        activity.recyclerView.scrollToPosition(activity.msgRAdapter.getItemCount() - 1);
                    }
                    break;
            }
        }
    }

    /****************************
     * http request - START
     ***************************/

    private void httpGetRemoveDevice() {
        String url = CommonUtil.formatUrl(Constants.SUF_DEVICE_REMOVE);
        JSONObject commonRequest = CommonUtil.getCommonRequest();
        commonRequest.put("devID", mDeviceInfo.getDevID());
        String body = commonRequest.toJSONString();
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //TODO 删除成功
                String result = response.getString(BaseParser.ERROR_CODE);
                if (Constants.ERROR_CODE_SUCCEED.equals(result)) {
                    Intent intent = MainActivity.createIntent(DeviceControlActivity.this, MainActivity.TYPE_REMOVE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    showSnackBar(R.string.delete_failed);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
        JsonObjectRequest request = new JsonObjectRequest(url, body, listener, errorListener);
        request.setDescription("删除设备");
        VolleyHelper.addRequest(getApplicationContext(), request, requestTag);
    }

    public void sendCmd(String actionID, String inst, String infraTypeID) {
        if (!NetUtil.isNetWorkAvailable(getApplicationContext())) {
            showSnackBar(R.string.network_is_not_available);
            return;
        }
        if (cmdIsSending) // 指令正在发送
            return;
        cmdIsSending = true;
        showItemProgressbar(true);
        JsonObjectRequest request = makeInstRequest(actionID, inst, infraTypeID);
        VolleyHelper.addRequest(getApplicationContext(), request, requestTag);
    }

    public JsonObjectRequest makeInstRequest(String actionId, String inst, String infraTypeID) {
        //设备控制GET请求
        ///web/action?actionID=actionID&inst=inst&token=token
        String url = CommonUtil.formatUrl(String.format(Constants.SUF_DEVICE_CONTROL, actionId, inst, mDeviceInfo.getToken(), infraTypeID));
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String devStatus = response.getString("devStatus");
                String rtn = response.getString("errcode"); // 返回 A7B4直接显示，表示无相关的操作
                if (!Constants.ERROR_CODE_SUCCEED.equals(rtn) || DeviceTypeUtil.DEV_STATUS_A002.equals(devStatus)) {
                    showItemProgressbar(false);
                    parseResult(response.toJSONString());
                    cmdIsSending = false;
                } else {
                    showResult = response.toJSONString();
                    //等待12s,直接等待N5A0消息即可
                    mInterfaceHandler.sendEmptyMessageDelayed(CHECK_SEND_STATUS, 12 * 1000);
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 这里直接等待N5A0消息即可
                showItemWarnIcon(false);
                msgRAdapter.notifyItemChanged(msgRAdapter.getItemCount() - 1);
                cmdIsSending = false;
            }
        };
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener);
        RetryPolicy retryPolicy = new DefaultRetryPolicy(1500, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        jsonObjectRequest.setDescription("控制命令");
        return jsonObjectRequest;
    }

    private void httpPostDeviceQuery() {
        //SUF_DEVICE_QUERY
        String url = CommonUtil.formatUrl(Constants.SUF_DEVICE_QUERY);
        JSONObject commonRequest = CommonUtil.getCommonRequest();
        commonRequest.put("devID", mDeviceInfo.getDevID());
        String body = commonRequest.toJSONString();
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String result = response.getString(BaseParser.ERROR_CODE);
                if (Constants.ERROR_CODE_SUCCEED.equals(result)) {
                    mDeviceInfo = JSONObject.parseObject(response.toJSONString(), DeviceInfo.class);
                    mControllerPanelView.setPanelList(mDeviceInfo);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
        JsonObjectRequest request = new JsonObjectRequest(url, body, listener, errorListener);
        request.setDescription("单设备信息查询");
        VolleyHelper.addRequest(getApplicationContext(), request, requestTag);
    }
    /****************************
     * http request - END
     ****************************/
}
