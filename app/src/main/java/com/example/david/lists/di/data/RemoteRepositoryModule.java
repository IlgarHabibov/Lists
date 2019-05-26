package com.example.david.lists.di.data;

import com.example.david.lists.data.remote.IRemoteRepository;
import com.example.david.lists.data.remote.RemoteRepositoryImpl;
import com.example.david.lists.data.remote.UtilSnapshotListeners;
import com.example.david.lists.data.repository.IUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.example.david.lists.data.remote.RemoteRepositoryConstants.ITEMS_COLLECTION;
import static com.example.david.lists.data.remote.RemoteRepositoryConstants.USER_COLLECTION;
import static com.example.david.lists.data.remote.RemoteRepositoryConstants.USER_LISTS_COLLECTION;

@Module
final class RemoteRepositoryModule {
    @Singleton
    @Provides
    IRemoteRepository remoteDatabase(FirebaseFirestore firestore,
                                     @Named(USER_LISTS_COLLECTION) CollectionReference userListCollection,
                                     @Named(ITEMS_COLLECTION) CollectionReference itemCollection,
                                     UtilSnapshotListeners snapshotListeners) {
        return new RemoteRepositoryImpl(firestore, userListCollection, itemCollection, snapshotListeners);
    }

    @Singleton
    @Provides
    FirebaseFirestore firebaseFirestore(FirebaseFirestoreSettings settings) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.setFirestoreSettings(settings);
        return firestore;
    }

    @Singleton
    @Provides
    FirebaseFirestoreSettings firebaseFirestoreSettings() {
        final long cacheSize = 10 * 1024 * 1024; // 10mb
        return new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(cacheSize)
                .build();
    }

    @Singleton
    @Provides
    UtilSnapshotListeners utilSnapshotListeners(@Named(USER_LISTS_COLLECTION) CollectionReference userListCollection,
                                                @Named(ITEMS_COLLECTION) CollectionReference itemCollection,
                                                IUserRepository userRepository,
                                                FirebaseAuth firebaseAuth) {
        return new UtilSnapshotListeners(userListCollection, itemCollection, userRepository, firebaseAuth);
    }

    @Named(USER_LISTS_COLLECTION)
    @Singleton
    @Provides
    CollectionReference userListCollectionReference(@Named(USER_COLLECTION) DocumentReference userDocument) {
        return userDocument.collection(USER_LISTS_COLLECTION);
    }

    @Named(ITEMS_COLLECTION)
    @Singleton
    @Provides
    CollectionReference itemCollectionReference(@Named(USER_COLLECTION) DocumentReference userDocument) {
        return userDocument.collection(ITEMS_COLLECTION);
    }

    @Named(USER_COLLECTION)
    @Singleton
    @Provides
    DocumentReference userIdDocument(FirebaseFirestore firestore, FirebaseAuth auth) {
        String uid = auth.getCurrentUser().getUid();
        return firestore.collection(USER_COLLECTION).document(uid);
    }
}
