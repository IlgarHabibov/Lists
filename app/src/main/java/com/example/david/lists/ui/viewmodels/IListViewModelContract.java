package com.example.david.lists.ui.viewmodels;

import com.example.david.lists.datamodel.UserList;
import com.example.david.lists.ui.dialogs.EditingInfo;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

public interface IListViewModelContract {
    void userListClicked(UserList userList);

    void addButtonClicked();

    void add(String title);

    void swipedRight(int position);

    void changeTitle(int id, String newTitle);

    void swipedLeft(int position);

    void undoRecentDeletion();

    void deletionNotificationTimedOut();

    void refresh();

    RecyclerView.Adapter getAdapter();

    LiveData<String> getToolbarTitle();

    LiveData<UserList> getEventOpenUserList();

    LiveData<Boolean> getEventDisplayLoading();

    LiveData<String> getEventDisplayError();

    LiveData<String> getEventNotifyUserOfDeletion();

    LiveData<String> getEventAdd();

    LiveData<EditingInfo> getEventEdit();
}
