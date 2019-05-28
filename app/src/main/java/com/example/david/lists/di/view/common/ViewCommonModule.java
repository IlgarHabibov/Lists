package com.example.david.lists.di.view.common;

import android.app.Application;
import android.content.SharedPreferences;

import com.example.david.lists.application.ListsApplicationImpl;
import com.example.david.lists.data.repository.IRepository;
import com.example.david.lists.data.repository.IUserRepository;
import com.example.david.lists.di.data.AppComponent;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public final class ViewCommonModule {
    @Provides
    IRepository repository(AppComponent appComponent) {
        return appComponent.repository();
    }

    @Provides
    IUserRepository userRepository(AppComponent appComponent) {
        return appComponent.userRepository();
    }

    @Provides
    SharedPreferences sharedPreferences(AppComponent appComponent) {
        return appComponent.sharedPrefsNightMode();
    }

    @Provides
    AppComponent appComponent(Application application) {
        return ((ListsApplicationImpl) application).getAppComponent();
    }

    @ViewScope
    @Provides
    CompositeDisposable disposable() {
        return new CompositeDisposable();
    }
}
