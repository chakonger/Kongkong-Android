package cn.leanvision.normalkongkong.response.parser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import cn.leanvision.common.bean.ControllerPanelBean;
import cn.leanvision.common.bean.InfraConfig;
import cn.leanvision.normalkongkong.framework.response.parser.BaseParser;
import cn.leanvision.normalkongkong.response.InfraConfigResponse;

public class InfraConfigParser extends BaseParser<InfraConfigResponse> {
    @Override
    public InfraConfigResponse parse(String paramString) {
        JSONObject jbConfig = JSONObject.parseObject(paramString);
        InfraConfigResponse response = new InfraConfigResponse();

        resultBaseParser(response, jbConfig);

        /**解析空调面板*/
        response.mInfraConfig = new InfraConfig();
        response.mInfraConfig.setAUTO(jbConfig.getString("AUTO"));
        response.mInfraConfig.setCOLD(jbConfig.getString("COLD"));
        response.mInfraConfig.setFAN(jbConfig.getString("FAN"));
        response.mInfraConfig.setHEAT(jbConfig.getString("HEAT"));
        response.mInfraConfig.setWATER(jbConfig.getString("WATER"));
        response.mInfraConfig.setBigType(jbConfig.getString("bigType"));
        response.mInfraConfig.setFanDirection(jbConfig.getString("fanDirection"));
        response.mInfraConfig.setFanSpeed(jbConfig.getString("fanSpeed"));
        response.mInfraConfig.setDevSwitch(jbConfig.getString("switch"));
        JSONArray jsTemp = jbConfig.getJSONArray("temperature");
        if (jsTemp != null && jsTemp.size() >= 2) {
            response.mInfraConfig.setMinTemp((Integer) jsTemp.get(0));
            response.mInfraConfig.setMaxTemp((Integer) jsTemp.get(1));
        }

        /** 以下解析通用控制面板信息 */
        response.mControllerPanelBean = new ControllerPanelBean();
        JSONObject logoSet = jbConfig.getJSONObject("logoSet");
        if (logoSet != null) {
            HashMap<String, String> logoMap = new HashMap<>();
            logoMap.put("off", logoSet.getString("off"));
            logoMap.put("onl", logoSet.getString("onl"));
            logoMap.put("act", logoSet.getString("act"));
            response.mControllerPanelBean.setLogoSet(logoMap);
        }
        response.mControllerPanelBean.set_id(jbConfig.getString("_id"));
        response.mControllerPanelBean.setKeyRow(jbConfig.getIntValue("keyRow"));
        //定时的位置
        response.mControllerPanelBean.setTimer(jbConfig.getString("timer"));
        //电量的显示位置
        response.mControllerPanelBean.setMeter(jbConfig.getString("meter"));
        response.mControllerPanelBean.setKeyRowNum(jbConfig.getObject("keyRowNum", int[].class));

        ArrayList<Integer> keyList = new ArrayList<>();
        HashMap<String, ControllerPanelBean.ItemPanel> keysSet = new HashMap<>();
        ArrayList<ControllerPanelBean.ItemPanel> timerList = new ArrayList<>();
        JSONObject jsonObject = jbConfig.getJSONObject("keysSet");

        if (jsonObject != null) {
            Object[] keySet = jsonObject.keySet().toArray();
            Arrays.sort(keySet);
            for (int i = 0; i < keySet.length; i++) {
                String key = (String) keySet[i];
                keyList.add(Integer.parseInt(key));
                String tempStr = jsonObject.getString(key);
                ControllerPanelBean.ItemPanel mItemPanel = JSONObject.parseObject(tempStr, ControllerPanelBean.ItemPanel.class);
                keysSet.put(key, mItemPanel);
                if ("Y".equals(mItemPanel.getTimer().toUpperCase(Locale.CHINA))) {
                    timerList.add(mItemPanel);
                }
            }
        }

        response.mControllerPanelBean.setTimerList(timerList);
        response.mControllerPanelBean.setKeyList(keyList);
        response.mControllerPanelBean.setKeysSet(keysSet);
        response.mControllerPanelBean.setPanelType(jbConfig.getString("panelType"));
        response.mControllerPanelBean.setKeyCount(jbConfig.getIntValue("keyCount"));
        response.mControllerPanelBean.setKeyCol(jbConfig.getIntValue("keyCol"));
        response.mControllerPanelBean.setDirURL(jbConfig.getString("dirURL"));
        response.mControllerPanelBean.setTimerCom(jbConfig.getObject("timerCom", String[].class));
        return response;
    }
}
