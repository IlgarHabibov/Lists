package com.precopia.david.lists.view.preferences.buildlogic

import android.app.Application
import com.precopia.david.lists.common.buildlogic.ViewCommonModule
import com.precopia.david.lists.common.buildlogic.ViewScope
import com.precopia.david.lists.view.preferences.IPreferencesViewContract
import com.precopia.david.lists.view.preferences.PreferencesView
import dagger.BindsInstance
import dagger.Component

@ViewScope
@Component(modules = [
    PreferencesViewModule::class,
    ViewCommonModule::class
])
interface PreferencesViewComponent {
    fun inject(preferencesView: PreferencesView)

    @Component.Builder
    interface Builder {
        fun build(): PreferencesViewComponent

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun view(view: IPreferencesViewContract.View): Builder
    }
}