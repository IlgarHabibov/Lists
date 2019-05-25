package com.example.david.lists.ui.userlistlist;

import android.app.Application;
import android.content.SharedPreferences;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.david.lists.R;
import com.example.david.lists.data.datamodel.UserList;
import com.example.david.lists.data.repository.IRepository;
import com.example.david.lists.util.SingleLiveEvent;
import com.example.david.lists.util.UtilExceptions;
import com.example.david.lists.util.UtilNightMode;
import com.example.david.lists.util.UtilUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subscribers.DisposableSubscriber;

public final class UserListViewModelImpl extends AndroidViewModel implements IUserListViewModel {

    private final MutableLiveData<List<UserList>> userLists;

    private final IRepository repository;
    private final CompositeDisposable disposable;

    private final MutableLiveData<Boolean> eventDisplayLoading;
    private final SingleLiveEvent<UserList> eventOpenUserList;
    private final SingleLiveEvent<Boolean> eventDisplayError;
    private final SingleLiveEvent<String> errorMessage;
    private final SingleLiveEvent<String> eventNotifyUserOfDeletion;
    private final SingleLiveEvent<Void> eventAdd;
    private final SingleLiveEvent<UserList> eventEdit;

    private final SingleLiveEvent<Void> eventSignOut;
    private final SingleLiveEvent<Void> eventConfirmSignOut;
    private final SingleLiveEvent<Void> eventSignIn;

    private final List<UserList> tempUserLists;
    private int tempUserListPosition;

    private final SharedPreferences sharedPrefs;

    UserListViewModelImpl(@NonNull Application application, IRepository repository, SharedPreferences sharedPrefs) {
        super(application);
        userLists = new MutableLiveData<>();
        this.repository = repository;
        disposable = new CompositeDisposable();
        eventOpenUserList = new SingleLiveEvent<>();
        eventDisplayLoading = new MutableLiveData<>();
        eventDisplayError = new SingleLiveEvent<>();
        errorMessage = new SingleLiveEvent<>();
        eventNotifyUserOfDeletion = new SingleLiveEvent<>();
        eventAdd = new SingleLiveEvent<>();
        eventEdit = new SingleLiveEvent<>();
        eventSignOut = new SingleLiveEvent<>();
        eventConfirmSignOut = new SingleLiveEvent<>();
        eventSignIn = new SingleLiveEvent<>();
        this.tempUserLists = new ArrayList<>();
        this.tempUserListPosition = -1;
        this.sharedPrefs = sharedPrefs;
        init();
    }

    private void init() {
        eventDisplayLoading.setValue(true);
        getAllUserLists();
    }


    private void getAllUserLists() {
        disposable.add(repository.getAllUserLists()
                .subscribeWith(userListsSubscriber())
        );
    }

    private DisposableSubscriber<List<UserList>> userListsSubscriber() {
        return new DisposableSubscriber<List<UserList>>() {
            @Override
            public void onNext(List<UserList> userLists) {
                UserListViewModelImpl.this.userLists.setValue(userLists);
                evaluateNewData(userLists);
            }

            @Override
            public void onError(Throwable t) {
                UtilExceptions.throwException(t);
                errorMessage.setValue(getStringResource(R.string.error_msg_generic));
                eventDisplayError.setValue(true);
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private void evaluateNewData(List<UserList> newUserListList) {
        eventDisplayLoading.setValue(false);

        if (newUserListList.isEmpty()) {
            errorMessage.setValue(getStringResource(R.string.error_msg_no_user_lists));
            eventDisplayError.setValue(true);
        } else {
            eventDisplayError.setValue(false);
        }
    }


    @Override
    public void userListClicked(UserList userList) {
        eventOpenUserList.setValue(userList);
    }

    @Override
    public void addButtonClicked() {
        eventAdd.call();
    }

    @Override
    public void edit(UserList userList) {
        eventEdit.setValue(userList);
    }


    @Override
    public void dragging(IUserListAdapter adapter, int fromPosition, int toPosition) {
        adapter.move(fromPosition, toPosition);
        Collections.swap(userLists.getValue(), fromPosition, toPosition);
    }

    @Override
    public void movedPermanently(int newPosition) {
        if (newPosition < 0) {
            return;
        }

        UserList userList = userLists.getValue().get(newPosition);
        repository.updateUserListPosition(
                userList,
                userList.getPosition(),
                newPosition
        );
    }

    @Override
    public void swipedLeft(IUserListAdapter adapter, int position) {
        delete(adapter, position);
    }


    @Override
    public void delete(IUserListAdapter adapter, int position) {
        adapter.remove(position);
        saveDeletedUserList(position);
        eventNotifyUserOfDeletion.setValue(
                getStringResource(R.string.message_user_list_deletion)
        );
    }

    private void saveDeletedUserList(int position) {
        tempUserLists.add(userLists.getValue().get(position));
        tempUserListPosition = position;
        userLists.getValue().remove(position);
    }


    @Override
    public void undoRecentDeletion(IUserListAdapter adapter) {
        if (tempUserLists.isEmpty() || tempUserListPosition < 0) {
            UtilExceptions.throwException(new UnsupportedOperationException(
                    getStringResource(R.string.error_invalid_action_undo_deletion)
            ));
        }
        reAdd(adapter);
        deletionNotificationTimedOut();
    }

    private void reAdd(IUserListAdapter adapter) {
        int lastDeletedPosition = (tempUserLists.size() - 1);
        reAddUserListToAdapter(adapter, lastDeletedPosition);
        reAddUserListToLocalList(lastDeletedPosition);
        tempUserLists.remove(lastDeletedPosition);
    }

    private void reAddUserListToAdapter(IUserListAdapter adapter, int lastDeletedPosition) {
        adapter.reAdd(tempUserListPosition, tempUserLists.get(lastDeletedPosition));
    }

    private void reAddUserListToLocalList(int lastDeletedPosition) {
        userLists.getValue().add(tempUserListPosition, tempUserLists.get(lastDeletedPosition));
    }

    @Override
    public void deletionNotificationTimedOut() {
        if (tempUserLists.isEmpty()) {
            return;
        }
        repository.deleteUserLists(tempUserLists);
        tempUserLists.clear();
    }


    @Override
    public void nightMode(MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
            UtilNightMode.setDay();
            setNightModePreference(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            item.setChecked(true);
            UtilNightMode.setNight();
            setNightModePreference(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private void setNightModePreference(int mode) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getStringResource(R.string.night_mode_shared_pref_key), mode);
        editor.apply();
    }


    @Override
    public void signIn() {
        if (UtilUser.isAnonymous()) {
            eventSignIn.call();
        } else {
            UtilExceptions.throwException(new UnsupportedOperationException(
                    getStringResource(R.string.error_sign_in_when_not_anonymous)
            ));
        }
    }

    @Override
    public void signOutButtonClicked() {
        eventConfirmSignOut.call();
    }

    @Override
    public void signOut() {
        eventSignOut.call();
    }


    @Override
    public LiveData<List<UserList>> getUserLists() {
        List<UserList> value = userLists.getValue();
        if (value != null) {
            evaluateNewData(value);
        }
        return userLists;
    }

    @Override
    public LiveData<UserList> getEventOpenUserList() {
        return eventOpenUserList;
    }

    @Override
    public LiveData<Boolean> getEventDisplayLoading() {
        return eventDisplayLoading;
    }

    @Override
    public LiveData<Boolean> getEventDisplayError() {
        return eventDisplayError;
    }

    @Override
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    public LiveData<String> getEventNotifyUserOfDeletion() {
        return eventNotifyUserOfDeletion;
    }

    @Override
    public LiveData<Void> getEventAdd() {
        return eventAdd;
    }

    @Override
    public LiveData<UserList> getEventEdit() {
        return eventEdit;
    }

    @Override
    public LiveData<Void> getEventSignOut() {
        return eventSignOut;
    }

    @Override
    public LiveData<Void> getEventConfirmSignOut() {
        return eventConfirmSignOut;
    }

    @Override
    public LiveData<Void> getEventSignIn() {
        return eventSignIn;
    }


    private String getStringResource(int resId) {
        return getApplication().getString(resId);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}