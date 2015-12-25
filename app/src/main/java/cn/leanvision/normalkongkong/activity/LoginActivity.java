package cn.leanvision.normalkongkong.activity;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.mime.VolleyHelper;
import com.dd.processbutton.iml.ActionProcessButton;
import com.dd.processbutton.util.LvBaseGenerator;
import com.dd.processbutton.util.ProgressGenerator;
import com.dd.processbutton.util.SelfControllerProgressGenerator;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import cn.leanvision.common.util.CrcUtil;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.CommonUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;
import cn.leanvision.normalkongkong.framework.activity.LvBaseActivity;
import cn.leanvision.normalkongkong.framework.request.JsonObjectRequest;
import cn.leanvision.normalkongkong.framework.response.parser.BaseParser;
import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;

public class LoginActivity extends LvBaseActivity {

    @Bind(R.id.et_username)
    MaterialEditText etUsername;
    @Bind(R.id.et_pwd)
    MaterialEditText etPwd;
    @Bind(R.id.btnRegister)
    ActionProcessButton btnRegister;
    @Bind(R.id.btnSignIn)
    ActionProcessButton btnSignIn;
    @Bind(R.id.temp_et)
    EditText etTemp;

    private SharedPrefHelper sph;
    private LvBaseGenerator progressGenerator;

    @Override
    protected void setContentViewLv() {
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void initViewLv() {
        setupToolbar(R.string.title_activity_login);

        sph = SharedPrefHelper.getInstance();
        etUsername.setText(sph.getUserName());
        etPwd.setText(sph.getPwd());

        progressGenerator = new SelfControllerProgressGenerator(new ProgressGenerator.OnCompleteListener() {
            @Override
            public void onComplete() {
                //TODO
                LogUtil.log(getClass(), "onComplete");
            }
        });

        btnRegister.setMode(ActionProcessButton.Mode.ENDLESS);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                httpPostRegister();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                httpPostLogin();
            }
        });
    }

    @Override
    protected void afterInitView() {

    }

    /*****************************
     * http request zone - START
     ****************************/
    private void httpPostRegister() {
        //{"userName":"13912345678","passWord":"e10adc3949ba59abbe56e057f20f883e","appid":""}
        final String userName = etUsername.getText().toString().trim();
        final String pwd = etPwd.getText().toString().trim();
        if (StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(pwd)) {
            showSnackBar(R.string._n_register);
            return;
        }
        startRequest(false);

        String url = CommonUtil.formatUrl("web/regist");
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //解析注册返回
                //{"sessionID": "ce4900e46fe2c9286adf6fd695be94ddfee9ccb120150d4397af172b90c998abc493a0286bea304f25ed5c", "errcode": 0, "errmsg": "success"}
                String errcode = response.getString("errcode");
                if (Constants.ERROR_CODE_SUCCEED.equals(errcode)) {
                    String sessionID = response.getString("sessionID");
                    sph.saveSessionID(sessionID);
                    sph.saveUserName(userName);
                    sph.savePwd(pwd);

                    requestSucceed(false);
                } else {
                    requestFailed(false, response.getString(BaseParser.MSG));
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                requestFailed(false, getString(R.string.network_is_not_available));
            }
        };
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);
        jsonObject.put("passWord", CrcUtil.MD5(pwd));
        jsonObject.put("appid", Constants.APP_ID);
        String body = jsonObject.toJSONString();
        JsonObjectRequest request = new JsonObjectRequest(url, body, listener, errorListener);
        VolleyHelper.addRequest(this, request, requestTag);
    }

    //{"errcode": 130, "errmsg": "userName \u767b\u9646\u53f7/\u624b\u673a\u53f7\u7801\u5e94\u8be5\u4e3a11\u6570\u5b57"}
    private void httpPostLogin() {
        String url = CommonUtil.formatUrl("web/login");
        //{"userName":"13912345678","passWord":"e10adc3949ba59abbe56e057f20f883e"}
        final String userName = etUsername.getText().toString().trim();
        final String pwd = etPwd.getText().toString().trim();
        if (StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(pwd)) {
            showSnackBar(R.string._n_register);
            return;
        }
        startRequest(true);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);
        jsonObject.put("passWord", CrcUtil.MD5(pwd));
        String body = jsonObject.toJSONString();
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String errcode = response.getString(BaseParser.ERROR_CODE);
                if (Constants.ERROR_CODE_SUCCEED.equals(errcode)) {
                    String sessionID = response.getString("sessionID");
                    sph.saveSessionID(sessionID);
                    sph.saveUserName(userName);
                    sph.savePwd(pwd);

                    requestSucceed(true);
                } else {
                    showSnackBar(response.getString(BaseParser.MSG));
                    requestFailed(true, response.getString(BaseParser.MSG));
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                requestFailed(true, getString(R.string.network_is_not_available));
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(url, body, listener, errorListener);
        VolleyHelper.addRequest(this, request, requestTag);
    }

    /*****************************
     * http request zone - END
     ****************************/

    private void startRequest(boolean isLogin) {
        if (isLogin) {
            progressGenerator.start(btnSignIn);
        } else {
            progressGenerator.start(btnRegister);
        }

        etTemp.requestFocus();

        btnRegister.setEnabled(false);
        etUsername.setEnabled(false);
        etPwd.setEnabled(false);
        btnSignIn.setEnabled(false);
    }

    private void requestFailed(boolean isLogin, String content) {
        if (isLogin) {
            btnSignIn.setProgress(-1);
        } else {
            btnRegister.setProgress(-1);
        }

        showSnackBar(content);

        btnRegister.setEnabled(true);
        etUsername.setEnabled(true);
        etPwd.setEnabled(true);
        btnSignIn.setEnabled(true);
    }

    private void requestSucceed(boolean isLogin) {
        if (isLogin) {
            btnSignIn.setProgress(100);
        } else {
            btnRegister.setProgress(100);
        }

        Intent intent = MainActivity.createIntent(this);
        startActivity(intent);
        finish();
    }
}
