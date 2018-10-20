package com.example.david.lists.ui.viewmodels;

import android.app.Application;

import com.example.david.lists.R;
import com.example.david.lists.datamodel.UserList;
import com.example.david.lists.model.IModelContract;
import com.example.david.lists.ui.adapaters.UserListsAdapter;
import com.example.david.lists.ui.view.EditingInfo;
import com.example.david.lists.ui.view.ItemTouchHelperCallback;
import com.example.david.lists.util.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

import static com.example.david.lists.util.UtilRxJava.completableIoAccess;

public final class UserListViewModel extends AndroidViewModel
        implements IViewModelContract,
        ItemTouchHelperCallback.IStartDragListener {

    private final IModelContract model;
    private final CompositeDisposable disposable;

    private final List<UserList> userLists;
    private final UserListsAdapter adapter;
    private final ItemTouchHelper touchHelper;

    private final MutableLiveData<String> toolbarTitle;
    private final MutableLiveData<Boolean> eventDisplayLoading;
    private final SingleLiveEvent<UserList> eventOpenUserList;
    private final SingleLiveEvent<String> eventDisplayError;
    private final SingleLiveEvent<String> eventNotifyUserOfDeletion;
    private final SingleLiveEvent<String> eventAdd;
    private final SingleLiveEvent<EditingInfo> eventEdit;

    private List<UserList> tempUserLists;
    private int tempUserListPosition = -1;

    public UserListViewModel(@NonNull Application application, IModelContract model) {
        super(application);
        this.model = model;
        disposable = new CompositeDisposable();
        adapter = new UserListsAdapter(this, this);
        touchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(this));
        userLists = new ArrayList<>();
        toolbarTitle = new MutableLiveData<>();
        eventOpenUserList = new SingleLiveEvent<>();
        eventDisplayLoading = new MutableLiveData<>();
        eventDisplayError = new SingleLiveEvent<>();
        eventNotifyUserOfDeletion = new SingleLiveEvent<>();
        eventAdd = new SingleLiveEvent<>();
        eventEdit = new SingleLiveEvent<>();
        this.tempUserLists = new ArrayList<>();

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
                updateUserList(userLists);
                updateUi();
            }

            @Override
            public void onError(Throwable t) {
                Timber.e(t);
                eventDisplayError.setValue(
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
                    getStringResource(R.string.error_msg_no_user_lists)
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
        Timber.d("add - userLists size - %s", userLists.size());
        completableIoAccess(Completable.fromAction(() ->
                model.addList(new UserList(title, this.userLists.size())))
        );
    }


    @Override
    public void dragging(int fromPosition, int toPosition) {
        Collections.swap(userLists, fromPosition, toPosition);
        adapter.move(fromPosition, toPosition);
    }

    @Override
    public void movePermanently(int newPosition) {
        UserList userList = userLists.get(newPosition);
        completableIoAccess(Completable.fromAction(() ->
                model.moveUserListPosition(
                        userList.getId(),
                        userList.getPosition(),
                        newPosition
                ))
        );
    }


    @Override
    public void edit(int position) {
        eventEdit.setValue(new EditingInfo(userLists.get(position)));
    }

    @Override
    public void changeTitle(int listId, String newTitle) {
        completableIoAccess(Completable.fromAction(() ->
                model.changeListTitle(listId, newTitle))
        );
    }


    @Override
    public void delete(int position) {
        adapter.remove(position);
        tempUserLists.add(userLists.get(position));
        tempUserListPosition = position;

        eventNotifyUserOfDeletion.setValue(
                getStringResource(R.string.message_list_deletion)
        );
    }

    @Override
    public void swipedLeft(int position) {
        delete(position);
    }

    @Override
    public void undoRecentDeletion() {
        if (tempUserLists.isEmpty() || tempUserListPosition < 0) {
            throw new UnsupportedOperationException(
                    getStringResource(R.string.error_invalid_deletion_undo)
            );
        }
        reAdd();
    }

    private void reAdd() {
        int lastDeletedPosition = tempUserLists.size() - 1;
        adapter.reAdd(
                tempUserListPosition,
                tempUserLists.get(lastDeletedPosition)
        );
        tempUserLists.remove(lastDeletedPosition);
    }

    @Override
    public void deletionNotificationTimedOut() {
        // There is a possibility that tempUserLists is nullified,
        // before fromAction executes.
        List<Integer> userListsIds = new ArrayList<>(getUserListsIds());
        completableIoAccess(Completable.fromAction(() ->
                model.deleteList(userListsIds))
        );
        tempUserLists.clear();
    }

    private List<Integer> getUserListsIds() {
        List<Integer> userListsIds = new ArrayList<>();
        for (UserList userList : tempUserLists) {
            userListsIds.add(userList.getId());
        }
        return userListsIds;
    }


    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
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
    public ItemTouchHelper getItemTouchHelper() {
        return touchHelper;
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