package cn.leanvision.normalkongkong.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.dd.processbutton.iml.ActionProcessButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import cn.leanvision.common.util.LvWifiAdminSimple;
import cn.leanvision.normalkongkong.LvApplication;
import cn.leanvision.normalkongkong.R;
import cn.leanvision.normalkongkong.framework.activity.LvBaseActivity;
import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;

/**
 * @author lvshicheng
 * @date 2015年12月22日11:40:30
 * @description wifi设置
 */
public class WifiSettingActivity extends LvBaseActivity {

    @Bind(R.id.et_wifi_ssid)
    MaterialEditText etWifiSsid;
    @Bind(R.id.et_wifi_pwd)
    MaterialEditText etWifiPwd;
    @Bind(R.id.btn_bound)
    ActionProcessButton btnBound;

    private LvWifiAdminSimple lvWifiAdminSimple;
    private SharedPrefHelper sph;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, WifiSettingActivity.class);
        return intent;
    }

    @Override
    protected void setContentViewLv() {
        setContentView(R.layout.activity_wifi_setting);

        lvWifiAdminSimple = LvWifiAdminSimple.getInstance(LvApplication.getInstance());
        if (!lvWifiAdminSimple.isWifiConnected()) {
            showSnackBar(R.string.wifi_not_open);
            finish();
        }
        sph = SharedPrefHelper.getInstance();
    }

    @Override
    protected void initViewLv() {
        setupToolbar(R.string.title_activity_wifi_setting, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String wifiConnectedSsid = lvWifiAdminSimple.getWifiConnectedSsid();
        etWifiSsid.setText(wifiConnectedSsid);
        etWifiSsid.setFocusable(false);
        etWifiPwd.setText(sph.getWifiPwd());

        btnBound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sph.saveWifiPwd(etWifiPwd.getText().toString());

                Intent intent = BoundActivity.createIntent(WifiSettingActivity.this, etWifiSsid.getText().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void afterInitView() {

    }
}