package com.example.david.lists.ui.viewmodels;

import android.app.Application;

import com.example.david.lists.R;
import com.example.david.lists.datamodel.UserList;
import com.example.david.lists.model.IModelContract;
import com.example.david.lists.ui.adapaters.UserListsAdapter;
import com.example.david.lists.ui.dialogs.EditingInfo;
import com.example.david.lists.util.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

final class UserListViewModel extends AndroidViewModel
        implements IListViewModelContract {

    private final IModelContract model;
    private final CompositeDisposable disposable;

    private final List<UserList> userLists;
    private final UserListsAdapter adapter;

    private final MutableLiveData<String> toolbarTitle;

    private final SingleLiveEvent<UserList> eventOpenUserList;
    private final SingleLiveEvent<Boolean> eventDisplayLoading;
    private final SingleLiveEvent<String> eventDisplayError;
    private final SingleLiveEvent<String> eventNotifyUserOfDeletion;
    private final SingleLiveEvent<String> eventAdd;
    private final SingleLiveEvent<EditingInfo> eventEdit;

    private UserList temporaryUserList;
    private int temporaryUserListPosition = -1;

    UserListViewModel(@NonNull Application application, IModelContract model) {
        super(application);
        this.model = model;
        disposable = new CompositeDisposable();
        adapter = new UserListsAdapter(this);
        userLists = new ArrayList<>();
        toolbarTitle = new MutableLiveData<>();
        eventOpenUserList = new SingleLiveEvent<>();
        eventDisplayLoading = new SingleLiveEvent<>();
        eventDisplayError = new SingleLiveEvent<>();
        eventNotifyUserOfDeletion = new SingleLiveEvent<>();
        eventAdd = new SingleLiveEvent<>();
        eventEdit = new SingleLiveEvent<>();

        init();
    }

    private void init() {
        toolbarTitle.setValue(getStringResource(R.string.app_name));
        eventDisplayLoading.setValue(true);
        getAllUserLists();
    }


    private void getAllUserLists() {
        disposable.add(model.getAllLists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(userListsSubscriber())
        );
    }

    private DisposableSubscriber<List<UserList>> userListsSubscriber() {
        return new DisposableSubscriber<List<UserList>>() {
            @Override
            public void onNext(List<UserList> userLists) {
                UserListViewModel.this.updateUserList(userLists);
                UserListViewModel.this.updateUi();
            }

            @Override
            public void onError(Throwable t) {
                Timber.e(t);
                UserListViewModel.this.eventDisplayError.setValue(
                        getStringResource(R.string.error_msg_generic)
                );
            }

            @Override
            public void onComplete() {

            }
        };
    }

    private void updateUserList(List<UserList> userLists) {
        this.userLists.clear();
        this.userLists.addAll(userLists);
    }

    private void updateUi() {
        if (userLists.isEmpty()) {
            eventDisplayError.setValue(
                    getStringResource(R.string.error_msg_empty_list)
            );
        } else {
            adapter.swapData(userLists);
            eventDisplayLoading.setValue(false);
        }
    }


    @Override
    public void userListClicked(UserList userList) {
        eventOpenUserList.setValue(userList);
    }

    @Override
    public void addButtonClicked() {
        eventAdd.setValue(getStringResource(R.string.hint_add_user_list));
    }

    @Override
    public void add(String title) {
        Completable.fromAction(() -> model.addList(new UserList(title, adapter.getItemCount())))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    @Override
    public void dragging(int fromPosition, int toPosition) {
        Collections.swap(userLists, fromPosition, toPosition);
        adapter.move(fromPosition, toPosition);
    }

    @Override
    public void movePermanently(int newPosition) {
        UserList userList = userLists.get(newPosition);
        Completable.fromAction(() -> model.moveUserListPosition(
                userList.getId(),
                userList.getPosition(),
                newPosition
        ))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    /**
     * Edit
     */
    @Override
    public void swipedRight(int position) {
        eventEdit.setValue(
                new EditingInfo(userLists.get(position))
        );
    }

    @Override
    public void changeTitle(int listId, String newTitle) {
        Completable.fromAction(() -> model.changeListTitle(listId, newTitle))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    /**
     * Delete
     */
    @Override
    public void swipedLeft(int position) {
        adapter.remove(position);
        temporaryUserList = userLists.get(position);
        temporaryUserListPosition = position;

        eventNotifyUserOfDeletion.setValue(
                getStringResource(R.string.message_list_deletion)
        );
    }

    @Override
    public void undoRecentDeletion() {
        if (temporaryUserList == null || temporaryUserListPosition < 0) {
            throw new UnsupportedOperationException(
                    getStringResource(R.string.error_invalid_deletion_undo)
            );
        }

        adapter.reAdd(temporaryUserListPosition, temporaryUserList);
        clearTemporary();
    }

    @Override
    public void deletionNotificationTimedOut() {
        // There is a possibility that temporaryUserList is nullified,
        // before fromAction executes.
        int id = temporaryUserList.getId();
        Completable.fromAction(() -> model.deleteList(id))
                .subscribeOn(Schedulers.io())
                .subscribe();

        clearTemporary();
    }

    private void clearTemporary() {
        Timber.d("clearTemporary");
        temporaryUserList = null;
        temporaryUserListPosition = -1;
    }


    @Override
    public void refresh() {
        Timber.i("refresh");
    }


    @Override
    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    @Override
    public LiveData<String> getToolbarTitle() {
        return toolbarTitle;
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
    public LiveData<String> getEventDisplayError() {
        return eventDisplayError;
    }

    @Override
    public LiveData<String> getEventNotifyUserOfDeletion() {
        return eventNotifyUserOfDeletion;
    }

    @Override
    public LiveData<String> getEventAdd() {
        return eventAdd;
    }

    @Override
    public LiveData<EditingInfo> getEventEdit() {
        return eventEdit;
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