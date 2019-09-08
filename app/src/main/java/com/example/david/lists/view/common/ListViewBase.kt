package com.example.david.lists.view.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.david.lists.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.Callback.*
import kotlinx.android.synthetic.main.list_view_base.*
import org.jetbrains.anko.toast
import javax.inject.Inject
import javax.inject.Provider

abstract class ListViewBase : Fragment(), TouchHelperCallback.MovementCallback {

    @Inject
    lateinit var layoutManger: Provider<LinearLayoutManager>

    @Inject
    lateinit var dividerItemDecorator: RecyclerView.ItemDecoration

    @Inject
    lateinit var itemTouchHelper: ItemTouchHelper

    protected abstract val title: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_view_base, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    protected abstract fun addButtonClicked()

    protected abstract fun undoRecentDeletion()

    protected abstract fun deletionNotificationTimedOut()

    protected abstract fun draggingListItem(fromPosition: Int, toPosition: Int)

    protected abstract fun permanentlyMoved(newPosition: Int)

    protected abstract fun enableUpNavigationOnToolbar(): Boolean

    protected abstract fun getAdapter(): RecyclerView.Adapter<*>


    private fun init() {
        initRecyclerView()
        initToolbar()
        initFab()
    }

    private fun initRecyclerView() {
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = layoutManger.get()
            addItemDecoration(dividerItemDecorator)
            itemTouchHelper.attachToRecyclerView(this)
            adapter = this@ListViewBase.getAdapter()
        }
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.apply {
            title = this@ListViewBase.title
            setDisplayHomeAsUpEnabled(enableUpNavigationOnToolbar())
        }
    }

    private fun initFab() {
        fab.setOnClickListener { addButtonClicked() }
        fabScrollListener()
    }

    private fun fabScrollListener() {
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    fab.hide()
                } else if (dy < 0) {
                    fab.show()
                }
            }
        })
    }

    protected fun notifyDeletionSnackbar(message: String) {
        Snackbar.make(root_layout, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.msg_undo) { undoRecentDeletion() }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        if (validSnackbarEvent(event)) {
                            deletionNotificationTimedOut()
                        }
                    }
                })
                .show()
    }

    private fun validSnackbarEvent(event: Int): Boolean {
        return (event == DISMISS_EVENT_TIMEOUT
                || event == DISMISS_EVENT_SWIPE
                || event == DISMISS_EVENT_MANUAL)
    }


    override fun dragging(fromPosition: Int, toPosition: Int) {
        draggingListItem(fromPosition, toPosition)
    }

    override fun movedPermanently(newPosition: Int) {
        permanentlyMoved(newPosition)
    }


    protected fun openDialogFragment(dialogFragment: DialogFragment) {
        dialogFragment.setTargetFragment(this, 0)
        dialogFragment.show(activity!!.supportFragmentManager, null)
    }


    protected fun displayLoading() {
        tv_error.isGone = true
        recycler_view.isGone = true
        fab.hide()

        progress_bar.isVisible = true
    }

    protected fun displayList() {
        progress_bar.isGone = true
        tv_error.isGone = true

        recycler_view.isVisible = true
        fab.show()
    }

    protected fun displayError(errorMessage: String) {
        progress_bar.isGone = true
        recycler_view.isGone = true
        fab.show()

        tv_error.text = errorMessage
        tv_error.isVisible = true
    }


    protected fun toastMessage(message: String) {
        context!!.toast(message)
    }
}