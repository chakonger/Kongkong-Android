package cn.leanvision.normalkongkong.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.mime.VolleyHelper;

import java.util.List;

import butterknife.Bind;
import cn.leanvision.common.bean.DeviceInfo;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.normalkongkong.CommonUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;
import cn.leanvision.normalkongkong.adapter.DeviceListAdapter;
import cn.leanvision.normalkongkong.framework.activity.LvBaseActivity;
import cn.leanvision.normalkongkong.framework.request.JsonObjectRequest;
import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;
import cn.leanvision.normalkongkong.receiver.RepeatReceiver;
import cn.leanvision.normalkongkong.service.LvKongCoreService;
import cn.leanvision.normalkongkong.service.util.LvBinderListener;
import cn.leanvision.normalkongkong.service.util.LvKongCoreServiceHandler;
import cn.leanvision.normalkongkong.util.PollingUtils;
import cn.leanvision.normalkongkong.widget.DividerItemDecoration;

/**
 * @author lvshicheng
 * @date 2015年12月23日10:07:26
 * @description 设备列表
 */
public class MainActivity extends LvBaseActivity {

    public static final int TYPE_BOUNDED = 1;
    public static final int TYPE_REMOVE  = 1;

    @Bind(R.id.root_swipe_view)
    SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.recycler_view)
    RecyclerView       recyclerView;

    private String            sessionID;
    private DeviceListAdapter deviceListAdapter;
    private RepeatReceiver    repeatReceiver;
    private BroadcastReceiver pushReceiver;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

    public static Intent createIntent(Context context, int type) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("type", type);
        return intent;
    }

    @Override
    protected void setContentViewLv() {
        setContentView(R.layout.activity_main);

        sessionID = SharedPrefHelper.getInstance().getSessionID();
        SharedPrefHelper.getInstance().setBackgroundTaskStop(false);
        SharedPrefHelper.getInstance().saveBackGroundGetUrl("");
    }

    @Override
    protected void initViewLv() {
        setupToolbar(R.string.title_activity_main);

        mRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);

        SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                httpGetDeviceList();
            }
        };
        mRefreshLayout.setOnRefreshListener(onRefreshListener);
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
            }
        });
        onRefreshListener.onRefresh();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initRecyclerView(List<DeviceInfo> deviceList) {
        if (deviceListAdapter == null) {
            deviceListAdapter = new DeviceListAdapter(getApplicationContext());
            deviceListAdapter.setItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int        position = (int) view.getTag();
                    DeviceInfo item     = deviceListAdapter.getItem(position);
                    Intent     intent   = DeviceControlActivity.createIntent(MainActivity.this, item);
                    startActivity(intent);
                }
            });
            deviceListAdapter.setDataList(deviceList);
            recyclerView.setAdapter(deviceListAdapter);

            recyclerView.setItemAnimator(new DefaultItemAnimator());
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST);
            recyclerView.addItemDecoration(dividerItemDecoration);
        } else {
            deviceListAdapter.setDataList(deviceList);
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void afterInitView() {
        httpGetDeviceList();

        startBackgroundRepeat();
        registerPushReceiver();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int type = intent.getIntExtra("type", -1);
        if (TYPE_BOUNDED == type) {
            httpGetDeviceList();
        } else if (TYPE_REMOVE == type) {
            httpGetDeviceList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = WifiSettingActivity.createIntent(this);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterRepeatReceiver();
        /**
         * 解绑后台服务
         * */
        LvKongCoreServiceHandler.getInstance().removeBind();
        SharedPrefHelper.getInstance().setBackgroundTaskStop(true);
        unregisterPushReceiver();
    }

    public void registerPushReceiver() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        pushReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Constants.BROADCAST_STATUS.equals(action)) {

                } else if (Constants.BROADCAST_BOUNDED.equals(action)) {
                    //TODO 删除本地被绑走的设备
                }
            }
        };
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_STATUS);
        filter.addAction(Constants.BROADCAST_BOUNDED);
        manager.registerReceiver(pushReceiver, filter);
    }

    public void unregisterPushReceiver() {
        if (pushReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.unregisterReceiver(pushReceiver);
            pushReceiver = null;
        }
    }

    /**
     * 开启后台服务
     */
    private void startBackgroundRepeat() {
        PollingUtils.stopPollingReceiver(this, RepeatReceiver.class, Constants.LV_ACTION_REPEATE);
        PollingUtils.startPollingReceiver(this, 60, null, Constants.LV_ACTION_REPEATE);
        registerRepeatReceiver();
    }

    private void registerRepeatReceiver() {
        LogUtil.log(getClass(), "registerRepeatReceiver");
        if (repeatReceiver == null) {
            LogUtil.log(getClass(), "registerRepeatReceiver first");
            repeatReceiver = new RepeatReceiver();
            IntentFilter filter = new IntentFilter(Constants.LV_ACTION_REPEATE);
            registerReceiver(repeatReceiver, filter);
            // start service first
            final LvKongCoreServiceHandler mCoreServiceUtil = LvKongCoreServiceHandler.getInstance();
            mCoreServiceUtil.registerCallBack(new LvBinderListener() {
                @Override
                public void bindSucceed(IBinder service, String param) {
                    LvKongCoreService.CoreBinder binder = (LvKongCoreService.CoreBinder) service;
                    binder.initBackGetThread(true);
                    mCoreServiceUtil.unregisterCallBack(this);
                }
            });
            mCoreServiceUtil.bindCoreBinder();
        }
    }

    public void unregisterRepeatReceiver() {
        if (repeatReceiver != null)
            unregisterReceiver(repeatReceiver);
    }

    /*****************************
     * http request zone - START
     ****************************/

    public void httpGetDeviceList() {
        //web/devtypelist
        //{" sessionID ":"xxxxxxxxxxxxxxx"}
        String url = CommonUtil.formatUrl("web/devicelist");
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<DeviceInfo> deviceList = JSONArray.parseArray(response.getString("device"), DeviceInfo.class);
                if (null != deviceList && deviceList.size() > 0) {
                    initRecyclerView(deviceList);
                } else {
                    if (null != deviceListAdapter) {
                        deviceListAdapter.clear();
                    }
                    showSnackBar(R.string._n_no_device);
                }
                mRefreshLayout.setRefreshing(false);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mRefreshLayout.setRefreshing(false);
                showSnackBar(R.string._n_refresh_failed);
            }
        };
        String            body    = String.format("{\"sessionID\":\"%s\"}", sessionID);
        JsonObjectRequest request = new JsonObjectRequest(url, body, listener, errorListener);
        VolleyHelper.addRequest(this, request, requestTag);
    }
    /*****************************
     * http request zone - END
     ****************************/
}
