package com.example.david.lists.view.itemlist.buldlogic

import androidx.recyclerview.widget.ItemTouchHelper
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.david.lists.common.buildlogic.ViewScope
import com.example.david.lists.util.ISchedulerProviderContract
import com.example.david.lists.view.itemlist.IItemViewContract
import com.example.david.lists.view.itemlist.ItemAdapter
import com.example.david.lists.view.itemlist.ItemListLogic
import com.example.david.lists.view.itemlist.ItemListViewModel
import com.example.domain.repository.IRepositoryContract
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
internal class ItemListViewModule {
    @ViewScope
    @Provides
    fun logic(view: IItemViewContract.View,
              viewModel: IItemViewContract.ViewModel,
              repo: IRepositoryContract.Repository,
              schedulerProvider: ISchedulerProviderContract,
              disposable: CompositeDisposable): IItemViewContract.Logic {
        return ItemListLogic(view, viewModel, repo, schedulerProvider, disposable)
    }

    @JvmSuppressWildcards
    @ViewScope
    @Provides
    fun viewModel(getStringRes: (Int) -> String,
                  getStringResArg: (Int, String) -> String,
                  userListId: String): IItemViewContract.ViewModel {
        return ItemListViewModel(getStringRes, getStringResArg, userListId)
    }

    @ViewScope
    @Provides
    fun adapter(logic: IItemViewContract.Logic,
                viewBinderHelper: ViewBinderHelper,
                itemTouchHelper: ItemTouchHelper): IItemViewContract.Adapter {
        return ItemAdapter(logic, viewBinderHelper, itemTouchHelper)
    }
}
