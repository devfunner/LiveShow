package com.liuyt.liveshow.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2017/3/19.
 */

public abstract class BaseActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flags = getWindowFlags();
        if(flags != -1) {
            //保持屏幕常亮
            getWindow().addFlags(flags);
        }
        setContentView(getLayoutId());
        initView();
        setupProcess();
    }

    /**
     * get windows flags
     * @return
     */
    protected int getWindowFlags(){
        return -1;
    }
    protected abstract int getLayoutId();
    protected abstract void initView();
    protected abstract void setupProcess();
}
