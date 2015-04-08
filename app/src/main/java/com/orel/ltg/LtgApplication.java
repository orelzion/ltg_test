package com.orel.ltg;

import android.app.Application;

/**
 * Created by yirmy on 08/04/2015.
 */
public class LtgApplication extends Application {

    private static LtgApplication mApplication;

    public LtgApplication() {}

    public static Application getApplication() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }
}
