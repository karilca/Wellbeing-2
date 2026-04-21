package com.guardian.circle.messaging;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.guardian.circle.data.FirestoreRepository;

public class GuardianFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        new FirestoreRepository(this).syncWearToken(token);
    }
}
