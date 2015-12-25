package cn.leanvision.normalkongkong.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.mime.VolleyHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.leanvision.common.bean.ControllerPanelBean;
import cn.leanvision.common.bean.DeviceInfo;
import cn.leanvision.common.bean.InfraConfig;
import cn.leanvision.common.bean.UniteDeviceInfo;
import cn.leanvision.common.util.AirCmdBuilder;
import cn.leanvision.common.util.CmdAction;
import cn.leanvision.common.util.DensityUtil;
import cn.leanvision.common.util.DeviceTypeUtil;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.CommonUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;
import cn.leanvision.normalkongkong.activity.DeviceControlActivity;
import cn.leanvision.normalkongkong.adapter.LvVerticalAdapter;
import cn.leanvision.normalkongkong.adapter.PanelAdapter;
import cn.leanvision.normalkongkong.request.ControlPanelRequest;
import cn.leanvision.normalkongkong.response.InfraConfigResponse;

/********************************
 * Created by lvshicheng on 15/12/22.
 * description 控制面板
 ********************************/
public class ControllerPanelView extends LinearLayout {

    private String requestTag = ControllerPanelView.class.getSimpleName();

    private final static String KEY_TIMER = "timer";
    private final static String KEY_METER = "meter";

    private int screenWidth;
    private int itemHeight;

    private FrameLayout flParentPanel;
    private TextView tvInfraName;
    private RecyclerView mRecyclerView;
    private LvVerticalViewGroup mLvVerticalViewGroup;
    private LvVerticalAdapter lvVerticalAdapter;
    private UniteDeviceInfo item;

    private View errorView;
    private View loadView;
    private DeviceInfo deviceInfo;
    private int initIndex = 0;

    private View viewHw;
    private View viewKg;
    private TextView tvSwitchOn;
    private ImageView ivSwitch;
    private ImageView ivMode;
    private TextView tvMode;
    private TextView tvTemp;
    private ImageView ivSpeed;
    private TextView tvSpeed;
    private ImageView ivDire;
    private TextView tvDire;

    private OnPanelViewItemClickListener mOnPanelViewItemClickListener;
    private PopupWindow popupWindow;
    private InfraConfig mInfraConfig;
    private Drawable blankDrawable;
    private ControllerPanelBean mControllerPanelBean;
    private String panelType;
    private ImageView ivKgSwitch;
    private TextView tvKgSwitchOn;

    public ControllerPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        blankDrawable = getResources().getDrawable(R.drawable.blank_drawable);

        screenWidth = DensityUtil.getScreenWidth(getContext());
        itemHeight = DensityUtil.dip2px(getContext(), 140);

        flParentPanel = (FrameLayout) findViewById(R.id.fl_parent_panel);
        tvInfraName = (TextView) findViewById(R.id.tv_infra_name);

        initRecyclerView();
        initErrorAndLoadingView();
    }

    private void initErrorAndLoadingView() {
        errorView = findViewById(R.id.tv_error);
        errorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();

                int currentItem = mLvVerticalViewGroup.getCurrentItem();
                item = lvVerticalAdapter.getItem(currentItem);
                displayPanel(currentItem, item);
            }
        });
        loadView = findViewById(R.id.progressbar);
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.controller_recycler_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        PanelAdapter panelAdapter = new PanelAdapter(getContext());
        mRecyclerView.setAdapter(panelAdapter);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getContext()));

        panelAdapter.setOnRecyclerItemClickListener(new PanelAdapter.OnRecyclerItemClickListener() {

            @Override
            public void onItemClicked(View v, int position) {
                DeviceControlActivity csdn = (DeviceControlActivity) getContext();
                if (csdn == null)
                    return;
                ControllerPanelBean.ItemPanel mItemPanel = ((PanelAdapter) mRecyclerView.getAdapter()).getItem(position);

                String inst = mItemPanel.getInst();
                String type = mItemPanel.getType();

                if ("url".equals(type)) {
                    String url = mItemPanel.getUrl();
                    if (StringUtil.isNotNull(url)) {
                        //TODO 跳转URL
                    }
                } else {
                    if (StringUtil.isNullOrEmpty(inst) || StringUtil.isNullOrEmpty(mItemPanel.getText()))
                        return;
                    if (KEY_TIMER.equals(inst)) { // turn to timer
                        String logo = String.format(Locale.CANADA, "%s%s", mControllerPanelBean.getDirURL(), mControllerPanelBean.getLogoSet().get("onl"));
                        final ArrayList<ControllerPanelBean.ItemPanel> timerList = mControllerPanelBean.getTimerList();
                        if (timerList != null && timerList.size() > 0) {
                            //TODO 跳转定时页面
                        }
                    } else if (KEY_METER.equals(inst)) {// turn to meter
                        //TODO 跳转电量
                    } else {
                        // 发送空静指令
                        sendInst(mItemPanel.getText(), CmdAction.CMD_ACTION_6, mItemPanel.getInst(), item.getInfraTypeID());
                    }
                }
            }
        });
    }

    private void addPanelItem(int keyCol, ArrayList<ControllerPanelBean.ItemPanel> list, String key, ControllerPanelBean.ItemPanel value) {
        if (StringUtil.isNullOrEmpty(key) || key.length() != 2)
            return;
        int keyRowTemp = Integer.parseInt(key.substring(0, 1));
        int keyColTemp = Integer.parseInt(key.substring(1));
        // 这里对号入座
        keyRowTemp -= 1;
        keyColTemp -= 1;
        int index = keyRowTemp * keyCol + keyColTemp;
        if (index < list.size()) {
            list.remove(list.get(index));
            list.add(index, value);
        }
    }

    // 控制面板的相关操作
    private void parsePanelInfo(InfraConfigResponse mConfigResponse) {
        // 空调红外的相关配置
        mInfraConfig = mConfigResponse.mInfraConfig;
        if (mInfraConfig != null)
            if (mInfraConfig.isSwitchSame()) // 乒乓键
            {
                // FIXME 对于乒乓键如何处理，提示是否需要？
                // showMsg(getResources().getString(R.string._words_open_and_close),
                // false);
            }

        // FIXME 控制面板信息 - 对于推送变更要做同样的
        mControllerPanelBean = mConfigResponse.mControllerPanelBean;
        if (mControllerPanelBean != null)
            if ("GENE".equals(mControllerPanelBean.getPanelType())) {
                showRecyclerView();
                panelType = mControllerPanelBean.getPanelType();

                int keyCol = mControllerPanelBean.getKeyCol();
                int keyRow = mControllerPanelBean.getKeyRow();
                ArrayList<ControllerPanelBean.ItemPanel> list = new ArrayList<>();
                // pre fill data
                // 11 21 23 31
                for (int i = 0; i < keyCol * keyRow; i++) {
                    ControllerPanelBean.ItemPanel itemPanel = new ControllerPanelBean.ItemPanel();
                    list.add(itemPanel);
                }

                HashMap<String, ControllerPanelBean.ItemPanel> keysSet = mControllerPanelBean.getKeysSet();
                List<Integer> keyList = mControllerPanelBean.getKeyList();
                for (int i = 0; i < keyList.size(); i++) {
                    String key = String.format(Locale.CHINA, "%d", keyList.get(i));
                    ControllerPanelBean.ItemPanel value = keysSet.get(key);

                    addPanelItem(keyCol, list, key, value);
                }

                GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), keyCol);
                mRecyclerView.setLayoutManager(gridLayoutManager);

                PanelAdapter mPanelAdapter = (PanelAdapter) mRecyclerView.getAdapter();
                mPanelAdapter.setImgBaseUrl(mControllerPanelBean.getDirURL());
                mPanelAdapter.setDataList(list);
                mPanelAdapter.notifyDataSetChanged();

                setFlLayout(keyRow);
            }
    }

    private void setFlLayout(int dpValue) {
        if (dpValue < 2)
            dpValue = 2;
        int height = DensityUtil.dip2px(getContext(), dpValue * 70);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth * 4 / 5, height);
        flParentPanel.setLayoutParams(params);
    }

    public void initWheelView() {
        if (mLvVerticalViewGroup == null) {
            mLvVerticalViewGroup = (LvVerticalViewGroup) findViewById(R.id.lv_vertical_view_group);

            mLvVerticalViewGroup.setOnItemChangedListener(new LvVerticalViewGroup.OnItemChangedListener() {
                @Override
                public void onItemChanged(int index) {
                    LogUtil.log(getClass(), "index : " + index);
                    if (lvVerticalAdapter != null && index >= 0 && index < lvVerticalAdapter.getCount()) {
                        item = lvVerticalAdapter.getItem(index);
                        displayPanel(index, item);
                    } else {
                        LogUtil.log(getClass(), "vertical view not find item");
                    }
                }
            });
        }

        if (lvVerticalAdapter == null) {
            lvVerticalAdapter = new LvVerticalAdapter(getContext());
            lvVerticalAdapter.setDataList(deviceInfo.getUniteList());

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth / 5, itemHeight);
            mLvVerticalViewGroup.setLayoutParams(params);
            mLvVerticalViewGroup.setViewHeight(itemHeight);
            mLvVerticalViewGroup.setAdapter(lvVerticalAdapter, initIndex);
        }
    }

    /**
     * 根据现有的信息决定显示的面板类型
     */
    private void displayPanel(int currentItem, UniteDeviceInfo item) {
        tvInfraName.setText(item.getInfraName());
//        LogUtil.log(getClass(), " ---- displayPanel ---- ");
        if (viewHw != null)
            viewHw.setVisibility(View.GONE);
        if (viewKg != null)
            viewKg.setVisibility(View.GONE);

        showLoading();

        if (DeviceTypeUtil.PANEL_TYPE_COMMON.equals(item.getPanelType())) {
            httpGetControllerInfo(currentItem, item.getInfraTypeID(), item.getInfraName());
        } else {
            // 非通用面板的panelType只有空调类型有用,用于限制相关操作
            if (DeviceTypeUtil.TYPE_KGHW.equals(item.getBigType())) {
                httpGetControllerInfo(currentItem, item.getInfraTypeID(), item.getInfraName());
                createHwPanel();
                setFlLayout(2);
            } else if (DeviceTypeUtil.TYPE_KG.equals(item.getBigType())) {
                createKgPanel(item);
                setFlLayout(2);
            } else {
                LogUtil.log(getClass(), "unknown panel type");
                // 这种也当做通用面板处理
                httpGetControllerInfo(currentItem, item.getInfraTypeID(), item.getInfraName());
            }
        }
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;

        initWheelView();
    }

    public void setLastInst(String actionId, String infraInst, String bigType, String infraTypeID) {
        List<UniteDeviceInfo> uniteList = lvVerticalAdapter.getDataList();
        for (int i = 0; i < uniteList.size(); i++) {
            UniteDeviceInfo familyPanelBean = uniteList.get(i);
            if (familyPanelBean.getInfraTypeID().equals(infraTypeID)) {
                if (CmdAction.CMD_ACTION_6.equals(actionId))
                    familyPanelBean.setLastInst(infraInst);
                else
                    familyPanelBean.setLastInst(actionId);
                break;
            }
        }

        if (item.getInfraTypeID().equals(infraTypeID)) {
            if (DeviceTypeUtil.TYPE_KG.equals(bigType)) {
                initKgPanelShow(actionId);
            } else if (DeviceTypeUtil.TYPE_KGHW.equals(bigType)) {
                initHwPanelShow(infraInst);
            } else {

            }
        }
    }


    private void createHwPanel() {
        if (mOnPanelViewItemClickListener == null)
            mOnPanelViewItemClickListener = new OnPanelViewItemClickListener((DeviceControlActivity) getContext(), this);

        if (viewHw == null) {
            viewHw = LayoutInflater.from(getContext()).inflate(R.layout.layout_panel_hw, null);

            viewHw.findViewById(R.id.rl_hw_switch).setOnClickListener(mOnPanelViewItemClickListener);
            viewHw.findViewById(R.id.ll_hw_time).setOnClickListener(mOnPanelViewItemClickListener);
            viewHw.findViewById(R.id.ll_hw_meter).setOnClickListener(mOnPanelViewItemClickListener);
            viewHw.findViewById(R.id.ll_hw_mode).setOnClickListener(mOnPanelViewItemClickListener);
            viewHw.findViewById(R.id.ll_hw_temp).setOnClickListener(mOnPanelViewItemClickListener);
            viewHw.findViewById(R.id.ll_hw_speed).setOnClickListener(mOnPanelViewItemClickListener);
            viewHw.findViewById(R.id.ll_hw_dire).setOnClickListener(mOnPanelViewItemClickListener);

            ivSwitch = (ImageView) viewHw.findViewById(R.id.iv_switch);
            tvSwitchOn = (TextView) viewHw.findViewById(R.id.tv_switch_on);
            ivMode = (ImageView) viewHw.findViewById(R.id.iv_mode);
            tvMode = (TextView) viewHw.findViewById(R.id.tv_mode);
            tvTemp = (TextView) viewHw.findViewById(R.id.tv_temp);
            ivSpeed = (ImageView) viewHw.findViewById(R.id.iv_speed);
            tvSpeed = (TextView) viewHw.findViewById(R.id.tv_speed);
            ivDire = (ImageView) viewHw.findViewById(R.id.iv_dire);
            tvDire = (TextView) viewHw.findViewById(R.id.tv_dire);
        }

        if (viewHw.getParent() == null) {
            flParentPanel.addView(viewHw);
        }

        String last_cmd = getLastInst();
        initHwPanelShow(last_cmd);

        hideAll();
        viewHw.setVisibility(View.VISIBLE);
    }

    private void createKgPanel(UniteDeviceInfo item) {
        if (mOnPanelViewItemClickListener == null)
            mOnPanelViewItemClickListener = new OnPanelViewItemClickListener((DeviceControlActivity) getContext(), this);
        if (viewKg == null) {
            viewKg = LayoutInflater.from(getContext()).inflate(R.layout.layout_panel_kg, null);
        }

        if (viewKg.getParent() == null) {
            flParentPanel.addView(viewKg);

            viewKg.findViewById(R.id.ll_kg_switch).setOnClickListener(mOnPanelViewItemClickListener);
            viewKg.findViewById(R.id.ll_kg_time).setOnClickListener(mOnPanelViewItemClickListener);
            viewKg.findViewById(R.id.ll_kg_power).setOnClickListener(mOnPanelViewItemClickListener);

            ivKgSwitch = (ImageView) viewKg.findViewById(R.id.iv_kg_switch);
            tvKgSwitchOn = (TextView) viewKg.findViewById(R.id.tv_kg_switch_on);
        }

        initKgPanelShow(getLastInst());
        ivKgSwitch.setTag(item.getLastInst());

        hideAll();
        viewKg.setVisibility(View.VISIBLE);
    }

    private void initKgPanelShow(String infraInst) {
        if (viewKg == null)
            return;
        if (StringUtil.isNullOrEmpty(infraInst))
            return;

        if (infraInst.length() == 1) {
            if (CmdAction.CMD_ACTION_OPEN.equals(infraInst)) {
                tvKgSwitchOn.setTextColor(getResources().getColor(R.color.primary_green));
                ivKgSwitch.setImageResource(R.drawable.new_switch_on);
            } else if (CmdAction.CMD_ACTION_CLOSE.equals(infraInst)) {
                tvKgSwitchOn.setTextColor(getResources().getColor(R.color.black));
                ivKgSwitch.setImageResource(R.drawable.new_switch_off);
            } else {
                LogUtil.log(getClass(), "what is this");
            }
        }
    }

    private void initHwPanelShow(String last_cmd) {
        LogUtil.log(getClass(), "last_inst : " + last_cmd);
        // 获取设备最后的指令状态
        if (viewHw == null) {
            return;
        }
        if (StringUtil.isNullOrEmpty(last_cmd) || last_cmd.length() != 6)
            last_cmd = "100800"; // 如果是空，则默认是插座的开
        // 根据获取的状态设置界面,区分开关类和红外类
        String tempCode;
        // 设置开关
        tempCode = last_cmd.substring(0, 1);
        if (AirCmdBuilder.CMD_OPEN_OR_CLOSE[0].equals(tempCode) || AirCmdBuilder.CMD_OPEN_OR_CLOSE[2].equals(tempCode)) {
            ivSwitch.setImageResource(R.drawable.new_switch_off);
            tvSwitchOn.setTextColor(getResources().getColor(R.color.black));
        } else {
            ivSwitch.setImageResource(R.drawable.new_switch_on);
            tvSwitchOn.setTextColor(getResources().getColor(R.color.primary_green));
        }

        // 设置模式
        tempCode = last_cmd.substring(1, 2);
        tvMode.setText(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_MODEL, tempCode));
        if (AirCmdBuilder.CMD_MODEL[0].equals(tempCode)) {
            ivMode.setBackgroundResource(R.drawable.new_auto);
        } else if (AirCmdBuilder.CMD_MODEL[1].equals(tempCode)) {
            ivMode.setBackgroundResource(R.drawable.new_cold);
        } else if (AirCmdBuilder.CMD_MODEL[2].equals(tempCode)) {
            ivMode.setBackgroundResource(R.drawable.new_hot);
        } else if (AirCmdBuilder.CMD_MODEL[3].equals(tempCode)) {
            ivMode.setBackgroundResource(R.drawable.new_delete_wet);
        } else if (AirCmdBuilder.CMD_MODEL[4].equals(tempCode)) {
            ivMode.setBackgroundResource(R.drawable.new_wind);
        }

        // 设置温度
        tempCode = last_cmd.substring(2, 4);
        String temp_str = AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_TEMP, tempCode);
        if (!StringUtil.isNullOrEmpty(temp_str)) {
            // 这里会出现空指针
            temp_str = String.format("%s℃", temp_str);
            tvTemp.setText(temp_str);
        }

        // 设置风速
        tempCode = last_cmd.substring(4, 5);
        String strCode = AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_WIND, tempCode);
        if (StringUtil.isNotNull(strCode))
            tvSpeed.setText(strCode);
        if (AirCmdBuilder.CMD_WIND[0].equals(tempCode)) {
            ivSpeed.setBackgroundResource(R.drawable.new_auto);
        } else if (AirCmdBuilder.CMD_WIND[1].equals(tempCode)) {
            ivSpeed.setBackgroundResource(R.drawable.new_smallwind);
        } else if (AirCmdBuilder.CMD_WIND[2].equals(tempCode)) {
            ivSpeed.setBackgroundResource(R.drawable.new_middlewind);
        } else if (AirCmdBuilder.CMD_WIND[3].equals(tempCode)) {
            ivSpeed.setBackgroundResource(R.drawable.new_bigwind);
        }

        // 设置风向
        tempCode = last_cmd.substring(5, 6);
        strCode = AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_WIND_OREN, tempCode);
        if (StringUtil.isNotNull(strCode))
            tvDire.setText(strCode);
        if (AirCmdBuilder.CMD_WIND_OREN[0].equals(tempCode)) {
            ivDire.setBackgroundResource(R.drawable.new_auto);
        } else if (AirCmdBuilder.CMD_WIND_OREN[1].equals(tempCode)) {
            ivDire.setBackgroundResource(R.drawable.new_wind_low);
        } else if (AirCmdBuilder.CMD_WIND_OREN[2].equals(tempCode)) {
            ivDire.setBackgroundResource(R.drawable.new_wind_middle);
        } else if (AirCmdBuilder.CMD_WIND_OREN[3].equals(tempCode)) {
            ivDire.setBackgroundResource(R.drawable.new_wind_high);
        }
    }

    private static String translateInstToString(String inst) {
        StringBuilder result = new StringBuilder();
        String tempCode;
        tempCode = inst.substring(0, 1);
        result.append(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_OPEN_OR_CLOSE, tempCode));
        result.append("_");
        tempCode = inst.substring(1, 2);
        result.append(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_MODEL, tempCode));
        result.append("_");
        tempCode = inst.substring(2, 4);
        result.append(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_TEMP, tempCode));
        result.append("_");
        tempCode = inst.substring(4, 5);
        result.append(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_WIND, tempCode));
        result.append("_");
        tempCode = inst.substring(5, 6);
        result.append(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_WIND_OREN, tempCode));
        return result.toString();
    }

    public void recycle() {
        if (viewHw != null)
            viewHw.setOnClickListener(null);
        if (viewKg != null)
            viewKg.setOnClickListener(null);
        if (mOnPanelViewItemClickListener != null)
            mOnPanelViewItemClickListener = null;

        VolleyHelper.cancelAllRequests(getContext(), requestTag);
    }

    private void hideAll() {
        mRecyclerView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        loadView.setVisibility(View.GONE);
    }

    private void showRecyclerView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        loadView.setVisibility(View.GONE);
    }

    private void showError() {
        mRecyclerView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        loadView.setVisibility(View.GONE);
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        loadView.setVisibility(View.VISIBLE);
    }

    public UniteDeviceInfo getItem() {
        int currentItem = mLvVerticalViewGroup.getCurrentItem();
        currentItem = currentItem < 0 ? 0 : currentItem;
        return lvVerticalAdapter.getItem(currentItem);
    }

    /**
     * 发送控制命令
     */
    private void sendInst(String showText, String actionId, String inst, String infraTypeID) {
        DeviceControlActivity cdn = (DeviceControlActivity) getContext();
        cdn.displayMimeText(showText);
        cdn.sendCmd(actionId, inst, infraTypeID);
    }

    /**************************
     * http request - START
     *************************/
    private void httpGetControllerInfo(final int position, String infraTypeID, String infraName) {
        String url = CommonUtil.formatUrl(Constants.SUF_INFRA_QUERY);
        JSONObject commonRequest = CommonUtil.getCommonRequest();
        commonRequest.put("infraTypeID", infraTypeID);
        String body = commonRequest.toJSONString();

        Response.Listener<InfraConfigResponse> listener = new Response.Listener<InfraConfigResponse>() {
            @Override
            public void onResponse(InfraConfigResponse mConfigResponse) {
                if (position != mLvVerticalViewGroup.getCurrentItem())
                    return;
                if (Constants.ERROR_CODE_SUCCEED.equals(mConfigResponse.RTN)) {
                    parsePanelInfo(mConfigResponse);
                } else {
                    showError();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (mLvVerticalViewGroup.getCurrentItem() != position)
                    return;
                showError();
            }
        };
        ControlPanelRequest request = new ControlPanelRequest(url, body, listener, errorListener);
        request.setDescription("控制面板查询");
        request.setCacheTime(Constants.CACHE_TIME);
        request.setShouldCache(Constants.CACHE_ENABLE);
        VolleyHelper.addRequest(getContext(), request, requestTag);
    }
    /**************************
     * http request - END
     *************************/
    /**
     * 获取当前面板的最后一条指令
     */
    public String getLastInst() {
        int currentItem = mLvVerticalViewGroup.getCurrentItem();
        if (currentItem == -1)
            currentItem = 0;
        UniteDeviceInfo item = lvVerticalAdapter.getItem(currentItem);
        return item.getLastInst();
    }

    public void setPanelList(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        lvVerticalAdapter.setDataList(deviceInfo.getUniteList());
        lvVerticalAdapter.notifyDataSetChanged();
    }

    static class OnPanelViewItemClickListener implements OnClickListener {

        private WeakReference<DeviceControlActivity> activityWeakReference;
        private ControllerPanelView view;

        public OnPanelViewItemClickListener(DeviceControlActivity activity, ControllerPanelView view) {
            activityWeakReference = new WeakReference<>(activity);
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            DeviceControlActivity activity = activityWeakReference.get();
            if (activity == null)
                return;
            //TODO 开关和红外的itemClick事件处理
            switch (v.getId()) {
                case R.id.ll_kg_switch: //插座
                    String lastInst = view.getLastInst();
                    if (StringUtil.isNullOrEmpty(lastInst))
                        lastInst = "0"; // 默认是关
                    if (CmdAction.CMD_ACTION_CLOSE.equals(lastInst)) {
                        view.sendInst("开", CmdAction.CMD_ACTION_OPEN, "", view.item.getInfraTypeID());
                    } else {
                        view.sendInst("关", CmdAction.CMD_ACTION_CLOSE, "", view.item.getInfraTypeID());
                    }
                    break;
                case R.id.ll_kg_time:
                    //TODO 定时
                    break;
                case R.id.ll_hw_meter:
                case R.id.ll_kg_power:
                    //TODO 电量
                    break;
                case R.id.rl_hw_switch: //空调
                    lastInst = view.getLastInst();
                    if (StringUtil.isNullOrEmpty(lastInst))
                        lastInst = "100800"; // 默认是关
                    if (lastInst.startsWith("0")) {
                        String strShow = ControllerPanelView.translateInstToString(String.format("%s%s", "1", lastInst.substring(1)));
                        view.sendInst(strShow, CmdAction.CMD_ACTION_6, "1FFFFF", view.item.getInfraTypeID());
                    } else {
                        view.sendInst("关", CmdAction.CMD_ACTION_6, "0FFFFF", view.item.getInfraTypeID());
                    }
                    break;
                case R.id.ll_hw_time:
                    //TODO 定时
                    break;
                case R.id.ll_hw_mode:
                    view.showModePopupWindow(v);
                    break;
                case R.id.ll_hw_temp:
                    // 部分模式无法修改温度
                    if (!view.canTempTouch()) {
                        return;
                    }
                    view.showTempPopupWindow(v);
                    break;
                case R.id.ll_hw_speed:
                    view.showSpeedPopupWindow(v);
                    break;
                case R.id.ll_hw_dire:
                    view.showWindDirPopupWindow(v);
                    break;
                case R.id.ll_chat_modle_01:
                    view.ivMode.setBackgroundResource(R.drawable.new_auto);

                    String tempInst = null;
                    // 自动是否支持调温
                    if (view.mInfraConfig != null) {
                        int autoTemp = view.mInfraConfig.getAutoTemp();
                        // 暂时只处理温度的情况
                        if (-2 == autoTemp) {

                        } else if (-1 == autoTemp) {
                            // 此时直接继承上一次温度即可
                        } else {
                            tempInst = AirCmdBuilder.getCmdCode(AirCmdBuilder.INDEX_CMD_TEMP, String.format("%d", autoTemp));
                        }
                    }
                    view.modeControl(tempInst, 0);
                    break;
                case R.id.ll_chat_modle_02: // 模式制冷
                    view.ivMode.setBackgroundResource(R.drawable.new_cold);
                    view.modeControl(null, 1);
                    break;
                case R.id.ll_chat_modle_03: // 模式制热
                    view.ivMode.setBackgroundResource(R.drawable.new_hot);
                    view.modeControl(null, 2);
                    break;
                case R.id.ll_chat_modle_04: // 模式抽湿
                    view.ivMode.setBackgroundResource(R.drawable.new_delete_wet);
                    view.modeControl(null, 3);
                    break;
                case R.id.ll_chat_modle_05: // 模式送风
                    view.ivMode.setBackgroundResource(R.drawable.new_wind);
                    view.modeControl(null, 4);
                    break;
                case R.id.ll_temp_add:// 温度+
                    lastInst = view.getLastInst();
                    if (StringUtil.isNullOrEmpty(lastInst))
                        lastInst = "100800"; // 默认是关
                    Integer temp = Integer.parseInt(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_TEMP, lastInst.substring(2, 4)));

                    int maxTemp = 32;
                    if (view.mInfraConfig != null)
                        maxTemp = view.mInfraConfig.getMaxTemp();
                    if (temp < maxTemp) {
                        temp++;
                        view.tvTemp.setText(temp + "℃");
                    } else {
                        activity.showSnackBar(R.string.n_max_temp);
                    }
                    tempInst = AirCmdBuilder.getCmdCode(AirCmdBuilder.INDEX_CMD_TEMP, temp.toString());
                    String showText = ControllerPanelView.translateInstToString(String.format("1%s%s%s", lastInst.substring(1, 2), tempInst, lastInst.substring(4, 6)));
                    view.sendInst(showText, CmdAction.CMD_ACTION_6, String.format("1F%sFF", tempInst), view.item.getInfraTypeID());
                    break;
                case R.id.ll_temp_cut: // 温度-
                    lastInst = view.getLastInst();
                    if (StringUtil.isNullOrEmpty(lastInst))
                        lastInst = "100800"; // 默认是关
                    temp = Integer.parseInt(AirCmdBuilder.getStrCode(AirCmdBuilder.INDEX_CMD_TEMP, lastInst.substring(2, 4)));

                    int minTemp = 16;
                    if (view.mInfraConfig != null)
                        minTemp = view.mInfraConfig.getMinTemp();
                    if (temp > minTemp) {
                        temp--;
                        view.tvTemp.setText(temp + "℃");
                    } else {
                        activity.showSnackBar(R.string.n_max_temp);
                    }
                    tempInst = AirCmdBuilder.getCmdCode(AirCmdBuilder.INDEX_CMD_TEMP, temp.toString());
                    showText = ControllerPanelView.translateInstToString(String.format("1%s%s%s", lastInst.substring(1, 2), tempInst, lastInst.substring(4, 6)));
                    view.sendInst(showText, CmdAction.CMD_ACTION_6, String.format("1F%sFF", tempInst), view.item.getInfraTypeID());
                    break;
                case R.id.ll_wind_auto: // 风速自动
                    view.ivSpeed.setBackgroundResource(R.drawable.new_auto);
                    view.speedControl(0);
                    break;
                case R.id.ll_wind_small:
                    view.ivSpeed.setBackgroundResource(R.drawable.new_smallwind);
                    view.speedControl(1);
                    break;
                case R.id.ll_wind_middle:
                    view.ivSpeed.setBackgroundResource(R.drawable.new_middlewind);
                    view.speedControl(2);
                    break;
                case R.id.ll_wind_big:
                    view.ivSpeed.setBackgroundResource(R.drawable.new_bigwind);
                    view.speedControl(3);
                    break;
                case R.id.ll_windoption_auto: // 风向自动
                    view.ivDire.setBackgroundResource(R.drawable.new_auto);
                    view.windDirControl(0);
                    break;
                case R.id.ll_windoption_low:
                    view.ivDire.setBackgroundResource(R.drawable.new_wind_low);
                    view.windDirControl(1);
                    break;
                case R.id.ll_windoption_middle:
                    view.ivDire.setBackgroundResource(R.drawable.new_wind_middle);
                    view.windDirControl(2);
                    break;
                case R.id.ll_windoption_high:
                    view.ivDire.setBackgroundResource(R.drawable.new_wind_high);
                    view.windDirControl(3);
                    break;
            }
        }
    }

    /**
     * 判断温度是否可以调
     */
    private boolean canTempTouch() {
        String last_cmd = getLastInst();
        boolean b = true;
        // 获取设备最后的指令状态
        if (mInfraConfig != null && StringUtil.isNotNull(last_cmd) && last_cmd.length() == 6) {
            String mode = last_cmd.substring(1, 2);
            int temp = -1;
            if ("0".equals(mode)) { // 自动
                temp = mInfraConfig.getAutoTemp();
            } else if ("3".equals(mode)) { // 抽湿
                temp = mInfraConfig.getWaterTemp();
            } else if ("4".equals(mode)) { // 送风
                temp = mInfraConfig.getFanTemp();
            } else {
            }

            b = -1 == temp || -2 == temp;
        }
        return b;
    }

    private void showPopWindow(View v) {
        popupWindow = new PopupWindow(v, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(blankDrawable);
    }

    /**
     * 模式的选项
     */
    public void showModePopupWindow(View v) {
        View pop_cha_model = LayoutInflater.from(getContext()).inflate(R.layout.pop_chat_modle, null);
        pop_cha_model.findViewById(R.id.ll_chat_modle_01).setOnClickListener(mOnPanelViewItemClickListener);
        pop_cha_model.findViewById(R.id.ll_chat_modle_02).setOnClickListener(mOnPanelViewItemClickListener);
        pop_cha_model.findViewById(R.id.ll_chat_modle_03).setOnClickListener(mOnPanelViewItemClickListener);
        pop_cha_model.findViewById(R.id.ll_chat_modle_04).setOnClickListener(mOnPanelViewItemClickListener);
        pop_cha_model.findViewById(R.id.ll_chat_modle_05).setOnClickListener(mOnPanelViewItemClickListener);

        showPopWindow(pop_cha_model);

        int offsetY = DensityUtil.dip2px(getContext(), 256);
        int offsetX = DensityUtil.dip2px(getContext(), 3);
        popupWindow.showAsDropDown(v, offsetX, -offsetY);
    }

    private void modeControl(String tempInst, int position) {
        popupWindow.dismiss();
        String lastInst = getLastInst();  // 这里挺奇怪，lastInst还会不是这个长度？
        if (StringUtil.isNullOrEmpty(lastInst) || lastInst.length() < 6) {
            lastInst = "100800"; // 默认是关
        }

        tvMode.setText(AirCmdBuilder.STR_CMD_MODEL[position]);

        String inst;
        String showText;

        if (StringUtil.isNotNull(tempInst)) {
            inst = String.format("1%s%sFF", AirCmdBuilder.CMD_MODEL[position], tempInst);
            showText = ControllerPanelView.translateInstToString(String.format("%s%s%s%s", AirCmdBuilder.CMD_OPEN_OR_CLOSE[1], AirCmdBuilder.CMD_MODEL[position], tempInst, lastInst.substring(4)));
        } else {
            inst = String.format("1%sFFFF", AirCmdBuilder.CMD_MODEL[position]);
            showText = ControllerPanelView.translateInstToString(String.format("%s%s%s", AirCmdBuilder.CMD_OPEN_OR_CLOSE[1], AirCmdBuilder.CMD_MODEL[position], lastInst.substring(2)));
        }
        sendInst(showText, CmdAction.CMD_ACTION_6, inst, item.getInfraTypeID());
    }

    /**
     * 温度的选项
     */
    public void showTempPopupWindow(View v) {
        View pop_cha_temp = LayoutInflater.from(getContext()).inflate(R.layout.pop_chat_temp, null);
        pop_cha_temp.findViewById(R.id.ll_temp_add).setOnClickListener(mOnPanelViewItemClickListener);
        pop_cha_temp.findViewById(R.id.ll_temp_cut).setOnClickListener(mOnPanelViewItemClickListener);

        showPopWindow(pop_cha_temp);

        int offsetY = DensityUtil.dip2px(getContext(), 136);
        int offsetX = DensityUtil.dip2px(getContext(), 3);
        popupWindow.showAsDropDown(v, offsetX, -offsetY);
    }

    /**
     * 风速的选项
     */
    public void showSpeedPopupWindow(View v) {
        View popWindSpeed = LayoutInflater.from(getContext()).inflate(R.layout.pop_chat_windspeed, null);
        popWindSpeed.findViewById(R.id.ll_wind_auto).setOnClickListener(mOnPanelViewItemClickListener);
        popWindSpeed.findViewById(R.id.ll_wind_big).setOnClickListener(mOnPanelViewItemClickListener);
        popWindSpeed.findViewById(R.id.ll_wind_middle).setOnClickListener(mOnPanelViewItemClickListener);
        popWindSpeed.findViewById(R.id.ll_wind_small).setOnClickListener(mOnPanelViewItemClickListener);

        showPopWindow(popWindSpeed);
        int offsetY = DensityUtil.dip2px(getContext(), 216);
        int offsetX = DensityUtil.dip2px(getContext(), 3);
        popupWindow.showAsDropDown(v, offsetX, -offsetY);
    }

    private void speedControl(int position) {
        popupWindow.dismiss();
        String lastInst = getLastInst();
        if (StringUtil.isNullOrEmpty(lastInst)) {
            lastInst = "100800"; // 默认是关
        }
        tvSpeed.setText(AirCmdBuilder.STR_CMD_WIND[position]);

        String inst;
        String showText;

        inst = String.format("1FFF%sF", AirCmdBuilder.CMD_WIND[position]);
        showText = ControllerPanelView.translateInstToString(String.format("%s%s%s%s", AirCmdBuilder.CMD_OPEN_OR_CLOSE[1], lastInst.substring(1, 4), AirCmdBuilder.CMD_WIND[position], lastInst.substring(5)));
        sendInst(showText, CmdAction.CMD_ACTION_6, inst, item.getInfraTypeID());
    }

    /**
     * 风向选项
     */
    public void showWindDirPopupWindow(View v) {
        View popWindDir = LayoutInflater.from(getContext()).inflate(R.layout.pop_chat_windoption, null);
        popWindDir.findViewById(R.id.ll_windoption_auto).setOnClickListener(mOnPanelViewItemClickListener);
        popWindDir.findViewById(R.id.ll_windoption_high).setOnClickListener(mOnPanelViewItemClickListener);
        popWindDir.findViewById(R.id.ll_windoption_middle).setOnClickListener(mOnPanelViewItemClickListener);
        popWindDir.findViewById(R.id.ll_windoption_low).setOnClickListener(mOnPanelViewItemClickListener);

        showPopWindow(popWindDir);
        int offsetY = DensityUtil.dip2px(getContext(), 216);
        int offsetX = DensityUtil.dip2px(getContext(), 3);
        popupWindow.showAsDropDown(v, offsetX, -offsetY);
    }

    public void windDirControl(int position) {
        popupWindow.dismiss();
        String lastInst = getLastInst();
        if (StringUtil.isNullOrEmpty(lastInst)) {
            lastInst = "100800"; // 默认是关
        }
        tvDire.setText(AirCmdBuilder.STR_CMD_WIND_OREN[position]);

        String inst;
        String showText;

        inst = String.format("1FFFF%s", AirCmdBuilder.CMD_WIND_OREN[position]);
        showText = ControllerPanelView.translateInstToString(String.format("%s%s%s", AirCmdBuilder.CMD_OPEN_OR_CLOSE[1], lastInst.substring(1, 5), AirCmdBuilder.CMD_WIND_OREN[position]));
        sendInst(showText, CmdAction.CMD_ACTION_6, inst, item.getInfraTypeID());
    }
}
