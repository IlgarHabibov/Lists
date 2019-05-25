package com.example.david.lists.ui.itemlist;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.david.lists.data.datamodel.Item;
import com.example.david.lists.di.view.itemfragment.DaggerItemsFragmentComponent;
import com.example.david.lists.ui.addedit.item.AddEditItemFragment;
import com.example.david.lists.ui.common.FragmentBase;

import javax.inject.Inject;
import javax.inject.Provider;

public class ItemsFragment extends FragmentBase {

    @Inject
    IItemViewModel viewModel;

    @Inject
    IItemAdapter adapter;
    @Inject
    Provider<LinearLayoutManager> layoutManger;
    @Inject
    Provider<RecyclerView.ItemDecoration> dividerItemDecorator;
    @Inject
    Provider<ItemTouchHelper> itemTouchHelper;

    private static final String ARG_KEY_USER_LIST_ID = "user_list_id_key";
    private static final String ARG_KEY_USER_LIST_TITLE = "user_list_title_key";

    public ItemsFragment() {
    }

    public static ItemsFragment newInstance(String userListId, String userListTitle) {
        ItemsFragment fragment = new ItemsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY_USER_LIST_ID, userListId);
        bundle.putString(ARG_KEY_USER_LIST_TITLE, userListTitle);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        inject();
        super.onAttach(context);
        observeViewModel();
    }

    private void inject() {
        DaggerItemsFragmentComponent.builder()
                .application(getActivity().getApplication())
                .fragment(this)
                .movementCallback(this)
                .userListId(getArguments().getString(ARG_KEY_USER_LIST_ID))
                .build()
                .inject(this);
    }

    private void observeViewModel() {
        observeItemList();
        observeEventDisplayLoading();
        observeEventDisplayError();
        observeEventNotifyUserOfDeletion();
        observeEventAdd();
        observeEventEdit();
        observeEventFinish();
    }

    private void observeItemList() {
        viewModel.getItemList().observe(this, items -> adapter.submitList(items));
    }

    private void observeEventDisplayError() {
        viewModel.getEventDisplayError().observe(this, display -> {
            if (display) {
                showError(viewModel.getErrorMessage().getValue());
            } else {
                hideError();
            }
        });
    }

    private void observeEventDisplayLoading() {
        viewModel.getEventDisplayLoading().observe(this, display -> {
            if (display) {
                showLoading();
            } else {
                hideLoading();
            }
        });
    }

    private void observeEventNotifyUserOfDeletion() {
        viewModel.getEventNotifyUserOfDeletion().observe(this, this::notifyDeletionSnackbar);
    }

    private void observeEventAdd() {
        viewModel.getEventAdd().observe(this, this::openAddDialog);
    }

    private void observeEventEdit() {
        viewModel.getEventEdit().observe(this, this::openEditDialog);
    }

    private void observeEventFinish() {
        viewModel.getEventFinish().observe(this, aVoid ->
                getActivity().getSupportFragmentManager().popBackStack()
        );
    }


    private void openAddDialog(String userListId) {
        openDialogFragment(
                AddEditItemFragment.getInstance("", "", userListId)
        );
    }

    private void openEditDialog(Item item) {
        openDialogFragment(
                AddEditItemFragment.getInstance(item.getId(), item.getTitle(), item.getUserListId())
        );
    }


    @Override
    protected void addButtonClicked() {
        viewModel.addButtonClicked();
    }

    @Override
    protected void undoRecentDeletion() {
        viewModel.undoRecentDeletion(adapter);
    }

    @Override
    protected void deletionNotificationTimedOut() {
        viewModel.deletionNotificationTimedOut();
    }

    @Override
    protected void draggingListItem(int fromPosition, int toPosition) {
        viewModel.dragging(adapter, fromPosition, toPosition);
    }

    @Override
    protected void permanentlyMoved(int newPosition) {
        viewModel.movedPermanently(newPosition);
    }

    @Override
    protected String getTitle() {
        return getArguments().getString(ARG_KEY_USER_LIST_TITLE);
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return (RecyclerView.Adapter) adapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManger() {
        return layoutManger.get();
    }

    @Override
    protected RecyclerView.ItemDecoration getDividerItemDecorator() {
        return dividerItemDecorator.get();
    }

    @Override
    protected ItemTouchHelper getItemTouchHelper() {
        return itemTouchHelper.get();
    }
}
