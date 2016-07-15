package in.lunchbox.firebase;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;
import java.util.Date;

import Model.User;
import Utils.Constants;
import Utils.ValidationUtil;

public class LoginActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static String mPhoneNumber = "";

    Integer mBackButtonCount = 0;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    View mFocusView = null;

    // Firebase reference
    Firebase ref;

    // Shared Preferences
    SharedPreferences mSharedPreferences;

    // login credentials
    String mEmail;
    String mPassword;
    String mToken;


    //  Request code used to invoke sign in user interactions for Google+
    public static final int RC_GOOGLE_LOGIN = 1;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // A flag indicating that a PendingIntent is in progress and prevents us from starting further intents.
    private boolean mGoogleIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve all issues preventing sign-in
     * without waiting. */
    private boolean mGoogleLoginClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can resolve them when the user clicks
     * sign-in. */
    private ConnectionResult mGoogleConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (null != getSupportActionBar()){
            getSupportActionBar().hide();
        }

        // set up Firebase
        Firebase.setAndroidContext(this);

        ref = new Firebase(Constants.FIREBASE_URL);
        mSharedPreferences = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // firebase sign in
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        if (mEmailSignInButton != null){
            mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }

        // google sign in
        Button mGoogleSignInButton = (Button) findViewById(R.id.google_sign_in_button);
        if (mGoogleSignInButton != null){
            mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //requestSMS();
                    attemptGoogleLogin();
                }
            });
        }


        // Setup the Google API object to allow Google+ logins /
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        if (mRegisterButton != null){
            mRegisterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registerUser();
                }
            });
        }

        // check if login credentials are available
        checkSharedPreferencesForLogin();

    }

    /**
     * Method to check for login credentials available in session and login automatically
     */
    private void checkSharedPreferencesForLogin() {
        mEmail = mSharedPreferences.getString("email", "");
        mPassword = mSharedPreferences.getString("password", "");
        mToken = mSharedPreferences.getString("token", "");

        if (!mToken.isEmpty()){
            /* Successfully got OAuth token from shared preferences, now login with Google */
            ref.authWithOAuthToken("google", mToken, new AuthResultHandler("google"));
        } else if (!mEmail.isEmpty() && !mPassword.isEmpty()){
            /* Successfully got firebase login credentials from shared preferences, now login with firebase */
            attemptLogin();
        }

    }

    /**
     * Attempts to sign in with the google account
     */
    private void attemptGoogleLogin(){
        mGoogleLoginClicked = true;
        if (!mGoogleApiClient.isConnecting()) {
            if (mGoogleConnectionResult != null) {
                resolveSignInError();
            } else if (mGoogleApiClient.isConnected()) {
                getGoogleOAuthTokenAndLogin();
            } else {
                    /* connect API now */
                Log.d(TAG, "Trying to connect to Google API");
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Register new user in firebase
     */
    private void registerUser() {

        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        intent.putExtra("email", mEmail);
        intent.putExtra("password", mPassword);
        startActivity(intent);

    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // if email is empty read from the input login form
        if (null == mEmail || mEmail.isEmpty()){
            mEmail = mEmailView.getText().toString();
            mPassword = mPasswordView.getText().toString();
        }

        boolean cancel = validateFields(mEmail, mPassword);

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mFocusView.requestFocus();
        } else {

            ref.authWithPassword(mEmail, mPassword, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("email", mEmail);
                    editor.putString("password", mPassword);
                    editor.apply();

                    navigateToNextActivity(authData);
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Log.e("Error", firebaseError.getMessage());
                    Toast.makeText(LoginActivity.this, "User authentication failed. Please check the user name and password entered.", Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Validate email and password fields for firebase login
     * @param email String
     * @param password String
     * @return boolean
     */
    private boolean validateFields(String email, String password){
        boolean cancel = false;

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);


        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            mFocusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        /*if (!TextUtils.isEmpty(password) && !ValidationUtil.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mFocusView = mEmailView;
            cancel = true;
        } else if (!ValidationUtil.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mFocusView = mEmailView;
            cancel = true;
        }

        return cancel;
    }

    /**
     * Get Google authentication token and sign in
     */
    private void getGoogleOAuthTokenAndLogin() {

        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                mToken = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    Account account = new Account(Plus.AccountApi.getAccountName(mGoogleApiClient), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                    mToken = GoogleAuthUtil.getToken(LoginActivity.this, account, scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return mToken;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    ref.authWithOAuthToken("google", token, new AuthResultHandler("google"));

                    // store token under shared preferences
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("token", token);
                    editor.apply();

                } else if (errorMessage != null) {

                    Log.e("Error", errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        };
        task.execute();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Connected with Google API, use this to authenticate with Firebase
        getGoogleOAuthTokenAndLogin();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!mGoogleIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button
            mGoogleConnectionResult = connectionResult;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            } else {
                Log.e(TAG, connectionResult.toString());
            }
        }
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {

            Log.i(TAG, provider + " auth successful");

            setAuthenticatedUser(authData);

            navigateToNextActivity(authData);

        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {

            Log.e("Error", firebaseError.getMessage());
            Toast.makeText(LoginActivity.this, "User authentication failed." + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void navigateToNextActivity(AuthData authData) {
        final String userID = authData.getUid();
        Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/" + "users");
        usersRef.orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getChildren() == null || dataSnapshot.getValue() == null){
                    // new user navigate to home
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                } else if (dataSnapshot.getChildren() != null){
                    User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    if (user.getType().equals("customer")){
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    } else if (user.getType().equals("admin")){
                        Intent intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
                        startActivity(intent);
                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Error finding userID", userID);
                Log.e("Error message", firebaseError.getMessage());
            }
        });
    }

    private void setAuthenticatedUser(AuthData authData) {
        final String userID = authData.getUid();
        final String name = (String)authData.getProviderData().get("displayName");
        Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/" + "users");
        usersRef.orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getChildren() == null || dataSnapshot.getValue() == null){
                    // user does not exist in firebase
                    // create new user
                    User user = new User(name, "", Long.parseLong("0"),
                            "", new Date(), new Date(), "active", Plus.AccountApi.getAccountName(mGoogleApiClient));
                    // set user id
                    user.setUserID(userID);
                    Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/" + "users");
                    Firebase userRef = usersRef.push();
                    userRef.setValue(user);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Error finding userID", userID);
                Log.e("Error message", firebaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // do nothing
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // close app
        if (mBackButtonCount >= 1){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Press the back button again to close the application.", Toast.LENGTH_SHORT).show();
            mBackButtonCount++;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_LOGIN) {
            /* This was a request by the Google API */
            if (resultCode != RESULT_OK) {
                mGoogleLoginClicked = false;
            }
            mGoogleIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Resolve google sign in errors
     */
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(this, RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

}
