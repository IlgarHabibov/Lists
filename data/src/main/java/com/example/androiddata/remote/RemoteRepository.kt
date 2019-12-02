package com.example.androiddata.remote

import com.example.androiddata.common.createCompletable
import com.example.domain.constants.RepositoryConstants.FIELD_ITEM_USER_LIST_ID
import com.example.domain.constants.RepositoryConstants.FIELD_POSITION
import com.example.domain.constants.RepositoryConstants.FIELD_TITLE
import com.example.domain.datamodel.Item
import com.example.domain.datamodel.UserList
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import io.reactivex.CompletableEmitter
import io.reactivex.Flowable

class RemoteRepository(private val firestore: FirebaseFirestore,
                       private val userListsCollection: CollectionReference,
                       private val itemsCollection: CollectionReference,
                       private val snapshotListener: IRemoteRepositoryContract.SnapshotListener) :
        IRemoteRepositoryContract.Repository {


    /**
     * OBSERVE
     */
    override fun getUserLists(): Flowable<List<UserList>> =
            snapshotListener.getUserListFlowable()

    override fun getItems(userListId: String) =
            snapshotListener.getItemFlowable(userListId)

    override val userListDeletedObservable: Flowable<List<UserList>>
        get() = snapshotListener.deletedUserListsFlowable


    /**
     * ADD
     */
    override fun addUserList(userList: UserList) = createCompletable {
        val documentRef = userListsCollection.document()
        val newUserList = UserList(userList.title, userList.position, documentRef.id)
        add(documentRef, newUserList, it)
    }

    override fun addItem(item: Item) = createCompletable {
        val documentRef = itemsCollection.document()
        val newItem = Item(item.title, item.position, item.userListId, documentRef.id)
        add(documentRef, newItem, it)
    }

    private fun add(docRef: DocumentReference, any: Any, emitter: CompletableEmitter) {
        docRef.set(any)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
    }


    /**
     * DELETE
     */
    override fun deleteUserLists(userListList: List<UserList>) =
            createCompletable { emitter ->
                firestore.runBatch {
                    for (userList in userListList) {
                        it.delete(getUserListDocument(userList.id))
                    }
                }.addOnSuccessListener { successfullyDeleteUserLists(emitter) }
                        .addOnFailureListener { emitter.onError(it) }
            }

    private fun successfullyDeleteUserLists(emitter: CompletableEmitter) =
            OnSuccessListener<Void> {
                userListsCollection
                        .orderBy(FIELD_POSITION, Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { reorderConsecutively(it, emitter) }
                        .addOnFailureListener { emitter.onError(it) }
            }

    override fun deleteItems(itemList: List<Item>) =
            createCompletable { emitter ->
                firestore.runBatch {
                    for (item in itemList) {
                        it.delete(getItemDocument(item.id))
                    }
                }.addOnSuccessListener(successfullyDeleteItems(itemList[0].userListId, emitter))
                        .addOnFailureListener { emitter.onError(it) }
            }

    private fun successfullyDeleteItems(groupId: String, emitter: CompletableEmitter) =
            OnSuccessListener<Void> {
                itemsCollection
                        .whereEqualTo(FIELD_ITEM_USER_LIST_ID, groupId)
                        .orderBy(FIELD_POSITION, Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { reorderConsecutively(it, emitter) }
                        .addOnFailureListener { emitter.onError(it) }
            }


    /**
     * RENAME
     */
    override fun renameUserList(userListId: String, newName: String) = createCompletable {
        rename(getUserListDocument(userListId), newName, it)
    }

    override fun renameItem(itemId: String, newName: String) = createCompletable {
        rename(getItemDocument(itemId), newName, it)
    }

    private fun rename(docRef: DocumentReference, newName: String, emitter: CompletableEmitter) {
        docRef.update(FIELD_TITLE, newName)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
    }


    /**
     * POSITION
     */
    override fun updateUserListPosition(userList: UserList, oldPos: Int, newPos: Int) =
            createCompletable {
                updatePosition(
                        getUserListDocument(userList.id),
                        oldPos,
                        newPos,
                        userListSuccessfullyUpdatedListener(it),
                        it
                )
            }

    private fun userListSuccessfullyUpdatedListener(emitter: CompletableEmitter) =
            OnSuccessListener<Void> {
                userListsCollection
                        .orderBy(FIELD_POSITION, Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { reorderConsecutively(it, emitter) }
                        .addOnFailureListener { emitter.onError(it) }
            }


    override fun updateItemPosition(item: Item, oldPos: Int, newPos: Int) =
            createCompletable {
                updatePosition(
                        getItemDocument(item.id),
                        oldPos,
                        newPos,
                        itemSuccessfullyUpdatedListener(item.userListId, it),
                        it
                )
            }

    private fun itemSuccessfullyUpdatedListener(userListId: String, emitter: CompletableEmitter) =
            OnSuccessListener<Void> {
                itemsCollection
                        .whereEqualTo(FIELD_ITEM_USER_LIST_ID, userListId)
                        .orderBy(FIELD_POSITION, Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { reorderConsecutively(it, emitter) }
                        .addOnFailureListener { emitter.onError(it) }
            }

    private fun updatePosition(docRef: DocumentReference,
                               oldPos: Int,
                               newPos: Int,
                               successListener: OnSuccessListener<in Void>,
                               emitter: CompletableEmitter) {
        when {
            positionsAreTheSame(oldPos, newPos) -> emitter.onComplete()
            positionsAreInvalid(oldPos, newPos) -> emitter.onError(
                    IllegalArgumentException("Positions cannot be less then 0")
            )
            else -> docRef.update(FIELD_POSITION, getNewTempPosition(oldPos, newPos))
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener { emitter.onError(it) }
        }
    }

    private fun positionsAreTheSame(oldPos: Int, newPos: Int) = oldPos == newPos

    private fun positionsAreInvalid(oldPos: Int, newPos: Int) = oldPos < 0 || newPos < 0

    private fun getNewTempPosition(oldPos: Int, newPos: Int) = when {
        newPos > oldPos -> newPos + 0.5
        else -> newPos - 0.5
    }


    /**
     * HELPER
     */
    private fun reorderConsecutively(querySnapshot: QuerySnapshot, emitter: CompletableEmitter) {
        firestore.runBatch {
            for ((index, snapshot) in querySnapshot.documents.withIndex()) {
                if (snapshot.getDouble(FIELD_POSITION) != index.toDouble()) {
                    it.update(snapshot.reference, FIELD_POSITION, index)
                }
            }
        }.addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
    }


    private fun getUserListDocument(groupId: String) = userListsCollection.document(groupId)

    private fun getItemDocument(itemId: String) = itemsCollection.document(itemId)
}