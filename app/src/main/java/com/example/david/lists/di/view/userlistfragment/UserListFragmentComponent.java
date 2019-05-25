package com.example.david.lists.di.view.userlistfragment;

import android.app.Application;

import androidx.fragment.app.Fragment;

import com.example.david.lists.di.view.common.RecyclerViewAdapterModule;
import com.example.david.lists.di.view.common.ViewCommonModule;
import com.example.david.lists.di.view.common.ViewScope;
import com.example.david.lists.view.common.TouchHelperCallback;
import com.example.david.lists.view.userlistlist.UserListsFragment;

import dagger.BindsInstance;
import dagger.Component;

@ViewScope
@Component(modules = {
        UserListFragmentModule.class,
        RecyclerViewAdapterModule.class,
        ViewCommonModule.class
})
public interface UserListFragmentComponent {
    void inject(UserListsFragment userListsFragment);

    @Component.Builder
    interface Builder {
        UserListFragmentComponent build();

        @BindsInstance
        Builder application(Application application);

        @BindsInstance
        Builder fragment(Fragment fragment);

        @BindsInstance
        Builder movementCallback(TouchHelperCallback.MovementCallback movementCallback);
    }
}
