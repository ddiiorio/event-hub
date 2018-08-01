package finalproject.comp3617.com.eventhub;

import android.app.Application;

import io.realm.Realm;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }

    static final class Constants {
        private static final String INSTANCE_ADDRESS = "eventhub.us1.cloud.realm.io";
        static final String AUTH_URL = "https://" + INSTANCE_ADDRESS + "/auth";
    }
}
