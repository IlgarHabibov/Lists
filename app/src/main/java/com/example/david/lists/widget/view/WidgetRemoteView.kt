package com.example.david.lists.widget.view

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.david.lists.R
import com.example.david.lists.widget.view.IWidgetRemoteViewContract.Companion.INTENT_ADAPTER
import com.example.david.lists.widget.view.IWidgetRemoteViewContract.Companion.INTENT_CONFIG
import com.example.david.lists.widget.view.IWidgetRemoteViewContract.Companion.INTENT_TITLE
import com.example.david.lists.widget.view.IWidgetRemoteViewContract.Companion.LIST_TITLE
import com.example.david.lists.widget.view.buildlogic.DaggerWidgetRemoteViewComponent
import javax.inject.Inject
import javax.inject.Named

class WidgetRemoteView(private val context: Context,
                       private val widgetId: Int) {

    init {
        DaggerWidgetRemoteViewComponent.builder()
                .context(context)
                .widgetId(widgetId)
                .build()
                .inject(this)
    }

    @Inject
    lateinit var remoteViews: RemoteViews

    @Inject
    @field:Named(LIST_TITLE)
    lateinit var listTitle: String

    @Inject
    @field:Named(INTENT_TITLE)
    lateinit var titleIntent: Intent

    @Inject
    @field:Named(INTENT_CONFIG)
    lateinit var configActivityIntent: Intent

    @Inject
    @field:Named(INTENT_ADAPTER)
    lateinit var adapterIntent: Intent


    fun updateWidget(): RemoteViews {
        initRemoveView()
        return remoteViews
    }

    private fun initRemoveView() {
        setTitle()
        setTitlePendingIntent()
        setConfigActivityPendingIntent()
        initRemoteAdapter()
    }

    private fun setTitle() {
        remoteViews.setTextViewText(R.id.widget_tv_title, listTitle)
    }

    private fun setTitlePendingIntent() {
        remoteViews.setOnClickPendingIntent(
                R.id.widget_tv_title,
                getPendingIntent(titleIntent)
        )
    }

    private fun setConfigActivityPendingIntent() {
        remoteViews.setOnClickPendingIntent(
                R.id.widget_iv_settings,
                getPendingIntent(configActivityIntent)
        )
    }

    private fun getPendingIntent(intent: Intent) = PendingIntent.getActivity(
            context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun initRemoteAdapter() {
        remoteViews.setRemoteAdapter(R.id.widget_list_view, adapterIntent)
        remoteViews.setEmptyView(R.id.widget_list_view, R.id.widget_tv_error)
    }
}
