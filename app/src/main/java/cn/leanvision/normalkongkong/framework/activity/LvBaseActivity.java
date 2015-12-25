package cn.leanvision.normalkongkong.framework.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.leanvision.normalkongkong.R;

/********************************
 * Created by lvshicheng on 15/12/21.
 * description
 ********************************/
public abstract class LvBaseActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    protected String requestTag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        requestTag = getClass().getSimpleName();
        setContentViewLv();
        ButterKnife.bind(this);
        initViewLv();
        afterInitView();
    }

    protected void setupToolbar(int title) {
        setupToolbar(title, null);
    }

    protected void setupToolbar(int title, View.OnClickListener listener) {
        setupToolbar(getResources().getString(title), listener);
    }

    protected void setupToolbar(String title) {
        setupToolbar(title, null);
    }

    protected void setupToolbar(String title, View.OnClickListener listener) {
        if (toolbar != null) {
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
            if (null != listener) {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
                toolbar.setNavigationOnClickListener(listener);
            }
        }
    }

    public void showSnackBar(final int resid) {
        if (Looper.myLooper() == getMainLooper()) {
            Snackbar.make(toolbar, resid, Snackbar.LENGTH_LONG).show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(toolbar, resid, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    public void showSnackBar(final String content) {
        if (Looper.myLooper() == getMainLooper()) {
            Snackbar.make(toolbar, content, Snackbar.LENGTH_LONG).show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(toolbar, content, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    protected abstract void setContentViewLv();

    protected abstract void initViewLv();

    protected abstract void afterInitView();
}
