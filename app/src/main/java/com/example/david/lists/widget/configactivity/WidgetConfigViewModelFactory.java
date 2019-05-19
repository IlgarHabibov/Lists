package com.example.david.lists.widget.configactivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.david.lists.application.MyApplication;
import com.example.david.lists.data.repository.IRepository;

final class WidgetConfigViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    @NonNull
    private final Application application;
    private final int widgetId;
    private final IRepository model;

    WidgetConfigViewModelFactory(@NonNull Application application, int widgetId) {
        super(application);
        this.application = application;
        this.widgetId = widgetId;
        this.model = ((MyApplication) application.getApplicationContext()).getAppComponent().repository();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WidgetConfigViewModel.class)) {
            //noinspection unchecked
            return (T) new WidgetConfigViewModel(application, model, widgetId);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
