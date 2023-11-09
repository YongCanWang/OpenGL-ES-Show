package com.trans.opengles;

import android.app.Application;

/**
 * @author Tom灿
 * @description:
 * @date :2023/11/6 16:27
 */
public class MyApplication extends Application {
    public static Application application;
    @Override
    public void onCreate() {
        super.onCreate();
        this.application = this;
    }
}
