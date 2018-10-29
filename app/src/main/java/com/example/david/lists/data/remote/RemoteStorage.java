package com.example.david.lists.data.remote;

import com.example.david.lists.data.datamodel.Item;
import com.example.david.lists.data.datamodel.UserList;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.example.david.lists.data.datamodel.DataModelFieldConstants.FIELD_ID;
import static com.example.david.lists.data.datamodel.DataModelFieldConstants.FIELD_ITEM_USER_LIST_ID;
import static com.example.david.lists.data.datamodel.DataModelFieldConstants.FIELD_POSITION;
import static com.example.david.lists.data.datamodel.DataModelFieldConstants.FIELD_TITLE;
import static com.example.david.lists.data.remote.RemoteDatabaseConstants.ITEMS_COLLECTION;
import static com.example.david.lists.data.remote.RemoteDatabaseConstants.USER_LISTS_COLLECTION;

public final class RemoteStorage implements IRemoteStorageContract {

    private final FirebaseFirestore firestore;
    private final CollectionReference userListsCollection;
    private final CollectionReference itemsCollection;

    private static RemoteStorage instance;

    public static IRemoteStorageContract getInstance() {
        if (instance == null) {
            instance = new RemoteStorage();
        }
        return instance;
    }

    private RemoteStorage() {
        firestore = FirebaseFirestore.getInstance();
        userListsCollection = firestore.collection(USER_LISTS_COLLECTION);
        itemsCollection = firestore.collection(ITEMS_COLLECTION);

        init();
    }


    private void init() {
        userListsCollection.addSnapshotListener(MetadataChanges.INCLUDE, userListsCollectionListener());
        itemsCollection.addSnapshotListener(MetadataChanges.INCLUDE, itemsCollectionListener());
    }

    private EventListener<QuerySnapshot> userListsCollectionListener() {
        return (queryDocumentSnapshots, e) -> {
//            Timber.i("userListsCollectionListener");

            if (e != null) {
                onFailure(e);
            }
            processMetadata(queryDocumentSnapshots.getMetadata());

            for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                Timber.i("Start of for-loop");
                switch (change.getType()) {
                    case ADDED:
                        Timber.i("Added");
                        break;
                    case MODIFIED:
                        Timber.i("Modified");
                        break;
                    case REMOVED:
                        Timber.i("Removed");
                        break;
                }
            }
        };
    }

    private EventListener<QuerySnapshot> itemsCollectionListener() {
        return (queryDocumentSnapshots, e) -> {
//            Timber.i("itemsCollectionListener");

            if (e != null) {
                onFailure(e);
            }
            processMetadata(queryDocumentSnapshots.getMetadata());

            for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                Timber.i("Start of for-loop");
                switch (change.getType()) {
                    case ADDED:
                        Timber.i("Added");
                        break;
                    case MODIFIED:
                        Timber.i("Modified");
                        break;
                    case REMOVED:
                        Timber.i("Removed");
                        break;
                }
            }
        };
    }

    private void processMetadata(SnapshotMetadata metadata) {
//        if (metadata.hasPendingWrites()) {
//            Timber.i("Pending writes");
//        } else {
//            Timber.i("Does NOT have pending writes");
//        }

//        Timber.i("Metadata - is from cache: %s", metadata.isFromCache());

        Timber.i("Has pending writes: %s", metadata.hasPendingWrites());
    }


    @Override
    public String addUserList(UserList userList) {
        DocumentReference documentRef = userListsCollection.document();
        String id = documentRef.getId();
        add(documentRef, new UserList(id, userList));
        return id;
    }

    @Override
    public String addItem(Item item) {
        final DocumentReference documentRef = itemsCollection.document();
        final String id = documentRef.getId();
        add(documentRef, new Item(id, item));
        return id;
    }

    private void add(DocumentReference documentRef, Object object) {
        documentRef.set(object)
                .addOnFailureListener(this::onFailure);
    }


    /**
     * Batch deletion of {@link UserList} and {@link Item} are separate
     * so I can easily refactor to Cloud Functions down the road.
     */
    @Override
    public void deleteUserLists(List<UserList> userLists) {
        List<String> userListIds = batchDeleteUserLists(userLists);
        prepareToBatchDeleteItems(userListIds);
    }

    private List<String> batchDeleteUserLists(List<UserList> userLists) {
        List<String> userListIds = new ArrayList<>();

        WriteBatch writeBatch = firestore.batch();
        for (UserList userList : userLists) {
            String id = userList.getId();
            userListIds.add(id);
            writeBatch.delete(getUserListDocument(id));
        }
        writeBatch.commit().addOnFailureListener(this::onFailure);

        return userListIds;
    }

    private void prepareToBatchDeleteItems(List<String> userListIds) {
        for (String userListId : userListIds) {
            itemsCollection
                    .whereEqualTo(FIELD_ID, userListId)
                    .get()
                    .addOnSuccessListener(this::batchDeleteItems)
                    .addOnFailureListener(this::onFailure);
        }
    }

    private void batchDeleteItems(QuerySnapshot queryDocumentSnapshots) {
        WriteBatch batch = firestore.batch();
        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
            batch.delete(snapshot.getReference());
        }
        batch.commit().addOnFailureListener(this::onFailure);
    }


    @Override
    public void deleteItems(List<Item> items) {
        WriteBatch batch = firestore.batch();
        for (Item item : items) {
            batch.delete(getItemDocument(item.getId()));
        }
        batch.commit().addOnFailureListener(this::onFailure);
    }


    @Override
    public void renameUserList(String userListId, String newName) {
        getUserListDocument(userListId)
                .update(FIELD_TITLE, newName)
                .addOnFailureListener(this::onFailure);
    }

    @Override
    public void renameItem(String itemId, String newName) {
        getItemDocument(itemId)
                .update(FIELD_TITLE, newName)
                .addOnFailureListener(this::onFailure);
    }


    @Override
    public void updateUserListPositionsDecrement(UserList userList, int oldPosition, int newPosition) {
        updateUserListPositions(
                userListsCollection,
                decrementPositions(getUserListDocument(userList.getId()), newPosition),
                oldPosition,
                newPosition
        );
    }

    @Override
    public void updateUserListPositionsIncrement(UserList userList, int oldPosition, int newPosition) {
        updateUserListPositions(
                userListsCollection,
                incrementPositions(getUserListDocument(userList.getId()), newPosition),
                oldPosition,
                newPosition
        );
    }

    private void updateUserListPositions(CollectionReference collectionReference, OnSuccessListener<QuerySnapshot> successListener, int oldPosition, int newPosition) {
        int lowerPosition = getLowerPosition(oldPosition, newPosition);
        int higherPosition = getHigherPosition(oldPosition, newPosition);
        collectionReference
                .whereGreaterThanOrEqualTo(FIELD_POSITION, lowerPosition)
                .whereLessThanOrEqualTo(FIELD_POSITION, higherPosition)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(this::onFailure);
    }

    @Override
    public void updateItemPositionsDecrement(Item item, int oldPosition, int newPosition) {
        updateItemPositions(
                item.getUserListId(),
                itemsCollection,
                decrementPositions(getItemDocument(item.getId()), newPosition),
                oldPosition,
                newPosition
        );
    }

    @Override
    public void updateItemPositionsIncrement(Item item, int oldPosition, int newPosition) {
        updateItemPositions(
                item.getUserListId(),
                itemsCollection,
                incrementPositions(getItemDocument(item.getId()), newPosition),
                oldPosition,
                newPosition
        );
    }

    private void updateItemPositions(String userListId, CollectionReference collectionReference, OnSuccessListener<QuerySnapshot> successListener, int oldPosition, int newPosition) {
        int lowerPosition = getLowerPosition(oldPosition, newPosition);
        int higherPosition = getHigherPosition(oldPosition, newPosition);
        collectionReference
                .whereEqualTo(FIELD_ITEM_USER_LIST_ID, userListId)
                .whereGreaterThanOrEqualTo(FIELD_POSITION, lowerPosition)
                .whereLessThanOrEqualTo(FIELD_POSITION, higherPosition)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(this::onFailure);
    }

    private OnSuccessListener<QuerySnapshot> decrementPositions(DocumentReference movedDocument, int newPosition) {
        return queryDocumentSnapshots -> {
            WriteBatch batch = firestore.batch();
            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                int updatedPosition = Objects.requireNonNull(snapshot.getLong(FIELD_POSITION)).intValue() - 1;
                batch.update(snapshot.getReference(), FIELD_POSITION, updatedPosition);
            }
            batch.update(movedDocument, FIELD_POSITION, newPosition);
            batch.commit().addOnFailureListener(this::onFailure);
        };
    }

    private OnSuccessListener<QuerySnapshot> incrementPositions(DocumentReference movedDocument, int newPosition) {
        return queryDocumentSnapshots -> {
            WriteBatch batch = firestore.batch();
            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                int updatedPosition = Objects.requireNonNull(snapshot.getLong(FIELD_POSITION)).intValue() + 1;
                batch.update(snapshot.getReference(), FIELD_POSITION, updatedPosition);
            }
            batch.update(movedDocument, FIELD_POSITION, newPosition);
            batch.commit().addOnFailureListener(this::onFailure);
        };
    }

    private int getLowerPosition(int oldPosition, int newPosition) {
        return oldPosition < newPosition ? oldPosition : newPosition;
    }

    private int getHigherPosition(int oldPosition, int newPosition) {
        return oldPosition > newPosition ? oldPosition : newPosition;
    }


    private DocumentReference getUserListDocument(String userListId) {
        return userListsCollection.document(userListId);
    }

    private DocumentReference getItemDocument(String itemId) {
        return itemsCollection.document(itemId);
    }


    private void onFailure(Exception exception) {
        Timber.e(exception);
    }
}
