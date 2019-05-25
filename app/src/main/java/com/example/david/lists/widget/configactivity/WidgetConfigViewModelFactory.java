package com.example.david.lists.widget.configactivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.david.lists.data.repository.IRepository;

public final class WidgetConfigViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    @NonNull
    private final Application application;
    private final IRepository repository;
    private final int widgetId;

    public WidgetConfigViewModelFactory(@NonNull Application application, IRepository repository, int widgetId) {
        super(application);
        this.application = application;
        this.repository = repository;
        this.widgetId = widgetId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WidgetConfigViewModelImpl.class)) {
            //noinspection unchecked
            return (T) new WidgetConfigViewModelImpl(application, repository, widgetId);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
