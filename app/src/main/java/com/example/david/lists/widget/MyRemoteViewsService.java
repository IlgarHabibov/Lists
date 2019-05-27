package com.example.david.lists.widget;

import android.annotation.SuppressLint;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.david.lists.R;
import com.example.david.lists.application.ListsApplicationImpl;
import com.example.david.lists.data.datamodel.Item;
import com.example.david.lists.data.repository.IRepository;
import com.example.david.lists.util.UtilExceptions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class MyRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MyRemoteViewsFactory(
                intent.getStringExtra(getApplication().getString(R.string.intent_extra_user_list_id)),
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                ((ListsApplicationImpl) getApplication()).getAppComponent().repository(),
                getApplication(),
                AppWidgetManager.getInstance(getApplicationContext())
        );
    }


    private class MyRemoteViewsFactory implements RemoteViewsFactory {

        private final List<Item> itemList;
        private final String userListId;

        private final IRepository repository;
        private final CompositeDisposable disposable;
        private final Application application;

        private final AppWidgetManager appWidgetManager;
        private final int widgetId;

        MyRemoteViewsFactory(String userListId, int widgetId, IRepository model, Application application, AppWidgetManager appWidgetManager) {
            this.application = application;
            itemList = new ArrayList<>();
            this.userListId = userListId;
            this.repository = model;
            this.disposable = new CompositeDisposable();
            this.appWidgetManager = appWidgetManager;
            this.widgetId = widgetId;
        }


        @Override
        public void onCreate() {
            disposable.add(repository.getItems(userListId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(itemListObserver())
            );
        }

        private DisposableSubscriber<List<Item>> itemListObserver() {
            return new DisposableSubscriber<List<Item>>() {
                @Override
                public void onNext(List<Item> newItemList) {
                    itemList.clear();
                    itemList.addAll(newItemList);
                    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_list_view);
                }

                @SuppressLint("LogNotTimber")
                @Override
                public void onError(Throwable t) {
                    UtilExceptions.throwException(t);
                }

                @Override
                public void onComplete() {

                }
            };
        }


        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews
                    = new RemoteViews(application.getPackageName(), R.layout.widget_list_item);
            initRemoteView(position, remoteViews);
            return remoteViews;
        }

        private void initRemoteView(int position, RemoteViews remoteViews) {
            remoteViews.setTextViewText(
                    R.id.widget_list_item_tv_title,
                    itemList.get(position).getTitle()
            );
        }


        @Override
        public void onDataSetChanged() {
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public void onDestroy() {
            disposable.clear();
        }
    }
}
