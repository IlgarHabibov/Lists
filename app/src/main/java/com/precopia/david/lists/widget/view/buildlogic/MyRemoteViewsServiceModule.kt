package com.precopia.david.lists.widget.view.buildlogic

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import com.precopia.david.lists.R
import com.precopia.david.lists.common.buildlogic.ViewScope
import com.precopia.david.lists.util.ISchedulerProviderContract
import com.precopia.david.lists.widget.view.MyRemoteViewsFactory
import com.precopia.domain.repository.IRepositoryContract
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Named

private const val PACKAGE_NAME = "package_name"
private const val USER_LIST_ID = "user_list_id"
private const val APP_WIDGET_ID = "app_widget_id"

@Module
class MyRemoteViewsServiceModule {
    @ViewScope
    @Provides
    fun remoteViewsFactory(@Named(PACKAGE_NAME) packageName: String,
                           @Named(USER_LIST_ID) userListId: String,
                           repo: IRepositoryContract.Repository,
                           disposable: CompositeDisposable,
                           appWidgetManager: AppWidgetManager,
                           @Named(APP_WIDGET_ID) appWidgetId: Int,
                           schedulerProvider: ISchedulerProviderContract): RemoteViewsService.RemoteViewsFactory {
        return MyRemoteViewsFactory(packageName, userListId, repo, disposable, appWidgetManager, appWidgetId, schedulerProvider)
    }

    @ViewScope
    @Provides
    @Named(PACKAGE_NAME)
    fun packageName(application: Application): String {
        return application.packageName
    }

    @ViewScope
    @Provides
    @Named(USER_LIST_ID)
    fun userListId(intent: Intent, application: Application): String {
        return intent.getStringExtra(application.getString(R.string.intent_extra_user_list_id))!!
    }

    @ViewScope
    @Provides
    fun appWidgetManager(application: Application): AppWidgetManager {
        return AppWidgetManager.getInstance(application)
    }

    @ViewScope
    @Provides
    @Named(APP_WIDGET_ID)
    fun appWidgetId(intent: Intent): Int {
        return intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }
}
