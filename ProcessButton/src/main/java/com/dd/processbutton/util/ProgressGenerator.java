package com.dd.processbutton.util;

import android.os.Handler;

import com.dd.processbutton.ProcessButton;

import java.util.Random;

/**
 * @description 改示例为随机时间完成process
 */
public class ProgressGenerator extends LvBaseGenerator{

    private OnCompleteListener mListener;
    private int mProgress;

    public ProgressGenerator(OnCompleteListener listener) {
        super(listener);
    }

    @Override
    public void start(final ProcessButton button) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgress += 10;
                button.setProgress(mProgress);
                if (mProgress < 100) {
                    handler.postDelayed(this, generateDelay());
                } else {
                    mListener.onComplete();
                }
            }
        }, generateDelay());
    }

    @Override
    public void doComplete(boolean isSucceed) {

    }

    private Random random = new Random();

    private int generateDelay() {
        return random.nextInt(1000);
    }
}
