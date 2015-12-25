package com.dd.processbutton.util;

import com.dd.processbutton.ProcessButton;

/**
 * @author lvshicheng
 * @description 自己控制结束时间, 仅限于用来处理无进度process
 */
public class SelfControllerProgressGenerator extends LvBaseGenerator {

    public SelfControllerProgressGenerator(OnCompleteListener listener) {
        super(listener);
    }

    @Override
    public void start(final ProcessButton button) {
        button.setProgress(50);
    }

    @Override
    public void doComplete(boolean isSucceed) {
        
    }
}
