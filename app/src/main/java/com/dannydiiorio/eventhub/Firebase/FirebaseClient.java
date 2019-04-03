package com.dannydiiorio.eventhub.Firebase;

import com.dannydiiorio.eventhub.App;
import com.dannydiiorio.eventhub.Model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danny Di Iorio
 * 2019-03-18
 */
public class FirebaseClient {
    private static final String TAG = "FirebaseClient";

    public FirebaseClient() {
    }

    private void writeNewUserInfo() {
        Map<String, String> userInfoMap = new HashMap<>();
        Map<String, Event> userEventsMap = new HashMap<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userInfoMap.put("name", user.getDisplayName());
            userInfoMap.put("email", user.getEmail());
            userInfoMap.put("photoUrl", user.getPhotoUrl().toString());
        }
        DatabaseReference mDatabase = App.Constants.database;
        mDatabase.child("users/").child(user.getUid()).setValue(userInfoMap);
        mDatabase.child("users/").child(user.getUid()).child("events").setValue(userEventsMap);
    }

    public final void registerNewUser() {
        writeNewUserInfo();
    }
}
