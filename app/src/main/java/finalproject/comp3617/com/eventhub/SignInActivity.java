package finalproject.comp3617.com.eventhub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.ObjectServerError;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

import static finalproject.comp3617.com.eventhub.App.Constants.AUTH_URL;

public class SignInActivity extends AppCompatActivity {
    private static final String FLAG = "FLAG";
    private EditText username, password;
    private Button signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        username = findViewById(R.id.usernameTxt);
        password = findViewById(R.id.passwordTxt);
        signInBtn = findViewById(R.id.signInBtn);

        if (SyncUser.current() != null) {
            SyncUser.current().logOut();
        }

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        // Reset errors
        username.setError(null);
        password.setError(null);
        // Store values at the time of the login attempt
        final String uname = username.getText().toString();
        final String pword = password.getText().toString();
        boolean createUser = true;
        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(uname)) {
            username.setError(getString(R.string.errorSignIn));
            focusView = username;
            cancel = true;
        }

        if (TextUtils.isEmpty(pword)) {
            password.setError(getString(R.string.errorSignIn));
            focusView = password;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            SyncCredentials credentials =
                    SyncCredentials.nickname(uname, true);
            SyncUser.logInAsync(credentials, AUTH_URL, new SyncUser.Callback<SyncUser>() {
                @Override
                public void onSuccess(SyncUser user) {
                    goToEventViewActivity();
                }

                @Override
                public void onError(ObjectServerError error) {
                    username.setError("Uh oh something went wrong!");
                    username.requestFocus();
                    Log.e("Login error", error.toString());
                }
            });
        }
    }

    private void goToEventViewActivity() {
        Intent i = new Intent(this, EventViewActivity.class);
        i.putExtra("signIn", FLAG);
        startActivity(i);
        finish();
    }
}
