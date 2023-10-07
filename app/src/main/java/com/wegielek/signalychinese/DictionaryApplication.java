package com.wegielek.signalychinese;

import android.app.Application;

public class DictionaryApplication extends Application {
    private static DictionaryApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        if(instance == null) {
            instance = this;
        }
    }

    public static DictionaryApplication getInstance() { return instance; }

}
