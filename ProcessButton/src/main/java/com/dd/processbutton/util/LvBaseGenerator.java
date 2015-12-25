package com.dd.processbutton.util;

import com.dd.processbutton.ProcessButton;

/********************************
 * Created by lvshicheng on 15/12/21.
 * description
 ********************************/
public abstract class LvBaseGenerator {

    protected ProgressGenerator.OnCompleteListener mListener;

    public LvBaseGenerator(OnCompleteListener listener) {
        mListener = listener;
    }

    public abstract void start(ProcessButton button);

    public abstract void doComplete(boolean isSucceed);

    public interface OnCompleteListener {

        void onComplete();
    }
}
