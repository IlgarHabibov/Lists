package com.example.david.lists.data.repository;

import com.example.david.lists.data.datamodel.Item;
import com.example.david.lists.data.datamodel.UserList;
import com.example.david.lists.data.remote.IRemoteRepositoryContract;
import com.example.david.lists.util.UtilExceptions;

import java.util.List;

import io.reactivex.Flowable;

public final class Repository implements IRepositoryContract.Repository {

    private final IRemoteRepositoryContract.Repository remote;


    public Repository(IRemoteRepositoryContract.Repository remoteDatabase) {
        remote = remoteDatabase;
    }


    @Override
    public Flowable<List<UserList>> getAllUserLists() {
        return remote.getUserLists();
    }

    @Override
    public Flowable<List<Item>> getItems(String userListId) {
        verifyValidStrings(userListId);
        return remote.getItems(userListId);
    }


    @Override
    public void addUserList(UserList userList) {
        validateObject(userList);
        remote.addUserList(userList);
    }

    @Override
    public void addItem(Item item) {
        validateObject(item);
        remote.addItem(item);
    }


    @Override
    public void deleteUserLists(List<UserList> userListList) {
        validateList(userListList);
        remote.deleteUserLists(userListList);
    }

    @Override
    public void deleteItems(List<Item> itemList) {
        validateList(itemList);
        remote.deleteItems(itemList);
    }


    @Override
    public void renameUserList(String userListId, String newTitle) {
        verifyValidStrings(userListId, newTitle);
        remote.renameUserList(userListId, newTitle);
    }

    @Override
    public void renameItem(String itemId, String newTitle) {
        verifyValidStrings(itemId, newTitle);
        remote.renameItem(itemId, newTitle);
    }


    @Override
    public void updateUserListPosition(UserList userList, int oldPosition, int newPosition) {
        if (positionNotChanged(oldPosition, newPosition)) {
            return;
        }
        validateObject(userList);
        validatePositions(oldPosition, newPosition);
        remote.updateUserListPosition(userList, oldPosition, newPosition);
    }

    @Override
    public void updateItemPosition(Item item, int oldPosition, int newPosition) {
        if (positionNotChanged(oldPosition, newPosition)) {
            return;
        }
        validateObject(item);
        validatePositions(oldPosition, newPosition);
        remote.updateItemPosition(item, oldPosition, newPosition);
    }

    private boolean positionNotChanged(int oldPosition, int newPosition) {
        return oldPosition == newPosition;
    }


    private void validateObject(Object object) {
        if (object == null) {
            nullObjectException();
        }
    }

    private void validateList(List list) {
        if (list == null) {
            nullObjectException();
        } else if (list.isEmpty()) {
            UtilExceptions.throwException(new IllegalArgumentException("Passed List is empty"));
        }
    }

    private void verifyValidStrings(String... stringArray) {
        for (String testingString : stringArray) {
            if (testingString == null || testingString.isEmpty()) {
                UtilExceptions.throwException(new IllegalArgumentException("This String is null or empty"));
            }
        }
    }

    private void validatePositions(int positionOne, int positionTwo) {
        if (positionOne < 0 || positionTwo < 0) {
            UtilExceptions.throwException(new IllegalArgumentException("One or both positions are less then 0"));
        }
    }


    @Override
    public Flowable<List<UserList>> getEventUserListDeleted() {
        return remote.getEventUserListDeleted();
    }


    private void nullObjectException() {
        UtilExceptions.throwException(new IllegalArgumentException("Parameter cannot be null"));
    }
}
