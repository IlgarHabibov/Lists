package com.example.david.lists.common;

import timber.log.Timber;

public final class ListsApplication extends ListsApplicationBase {
    @Override
    public void onCreate() {
        super.onCreate();
        initTimber();
    }

    private void initTimber() {
        Timber.plant(new Timber.DebugTree());
    }
}