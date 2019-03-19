package finalproject.comp3617.com.eventhub;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.realm.Realm;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }

    public static class Constants {
        private static final String INSTANCE_ADDRESS = "eventhub.us1.cloud.realm.io";
        static final String AUTH_URL = "https://" + INSTANCE_ADDRESS + "/auth";

        public static DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        public static FirebaseUser currentUser;
        public static GoogleSignInClient mGoogleSignInClient;
    }
}
