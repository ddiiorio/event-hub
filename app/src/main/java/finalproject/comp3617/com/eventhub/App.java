package finalproject.comp3617.com.eventhub;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }

    public static class Constants {
        public static DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        public static FirebaseUser currentUser;
        public static GoogleSignInClient mGoogleSignInClient;
        private static String pattern = "MMMM d yyyy";
        public static DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());

        protected static java.util.Date parseDate(String date) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
            } catch (ParseException e) {
                return null;
            }
        }

        protected static java.util.Date parseFirebaseDate(String date) {
            try {
                return new SimpleDateFormat(pattern, Locale.CANADA).parse(date);
            } catch (ParseException e) {
                return null;
            }
        }
    }
}
