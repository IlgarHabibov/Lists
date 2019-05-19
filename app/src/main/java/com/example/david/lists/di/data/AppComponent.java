package com.example.david.lists.di.data;

import android.app.Application;
import android.content.SharedPreferences;

import com.example.david.lists.data.repository.IRepository;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {
        RepositoryModule.class,
        SharedPreferencesModule.class
})
public interface AppComponent {
    IRepository repository();

    SharedPreferences sharedPrefsNightMode();

    @Component.Builder
    interface Builder {
        AppComponent build();

        @BindsInstance
        Builder application(Application application);
    }
}
