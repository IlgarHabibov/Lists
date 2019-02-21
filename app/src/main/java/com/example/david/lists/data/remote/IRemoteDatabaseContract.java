package com.example.david.lists.data.remote;

import com.example.david.lists.data.datamodel.Item;
import com.example.david.lists.data.datamodel.UserList;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.reactivex.Flowable;

public interface IRemoteDatabaseContract {
    Flowable<List<UserList>> getUserLists();

    Flowable<List<Item>> getItems(String userListId);

    void addUserList(UserList userList);

    void addItem(Item item);

    void deleteUserLists(List<UserList> userList);

    void deleteItems(List<Item> item);

    void renameUserList(String userListId, String newName);

    void renameItem(String itemId, String newName);

    void updateUserListPosition(UserList userList, int oldPosition, int newPosition);

    void updateItemPosition(Item item, int oldPosition, int newPosition);

    LiveData<List<UserList>> getEventUserListDeleted();
}