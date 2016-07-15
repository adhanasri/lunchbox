package in.lunchbox.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Date;
import java.util.Map;

import Model.User;
import Utils.CommonUtil;
import Utils.Constants;
import Utils.ValidationUtil;
import in.lunchbox.firebase.websync.CreateNewUserAPI;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    EditText mFirstNameView;
    EditText mLastNameView;
    EditText mPhoneView;
    EditText mAddressView;
    EditText mEmailView;
    EditText mPasswordView;
    Button mSubmitButton;
    View mFocusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        // set up register form
        mFirstNameView = (EditText) findViewById(R.id.firstName);
        mLastNameView = (EditText) findViewById(R.id.lastName);
        mPhoneView = (EditText) findViewById(R.id.phone);
        mAddressView = (EditText) findViewById(R.id.address);
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        // submit registration button
        mSubmitButton = (Button) findViewById(R.id.submit);

        // set email and password with values from the login screen
        mEmailView.setText(email);
        mPasswordView.setText(password);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

    }

    /**
     * Register user information with Firebase
     */
    private void registerUser(){
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        final String firstName = mFirstNameView.getText().toString();
        final String lastName = mLastNameView.getText().toString();
        final String phone = mPhoneView.getText().toString();
        final String address = mAddressView.getText().toString();

        boolean valid = validateFields(email, password, firstName, lastName, phone, address);

        if (!valid) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            mFocusView.requestFocus();
        } else {
            final Firebase ref = new Firebase(Constants.FIREBASE_URL);
            ref.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> stringObjectMap) {

                    User user = new User(firstName, lastName, CommonUtil.get10DigitPhone(phone),
                            address, new Date(), new Date(), "active", email);
                    // set user id
                    user.setUserID((String)stringObjectMap.entrySet().iterator().next().getValue());
                    Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/" + "users");
                    Firebase userRef = usersRef.push();
                    userRef.setValue(user);
                    Toast.makeText(RegisterActivity.this, "User Successfully registered", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginIntent);
                    // create user on web database
                    createUserOnWeb(user);
                }

                @Override
                public void onError(FirebaseError firebaseError) {

                }
            });
        }
    }

    /**
     * Validate registration form fields
     * @param email String
     * @param password String
     * @param firstName String
     * @param lastName String
     * @param phone String
     * @param address String
     * @return boolean
     */
    private boolean validateFields(String email, String password, String firstName, String lastName, String phone, String address) {
        boolean valid = true;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !ValidationUtil.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mFocusView = mPasswordView;
            valid = false;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mFocusView = mEmailView;
            valid = false;
        } else if (!ValidationUtil.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mFocusView = mEmailView;
            valid = false;
        }

        // Check for valid address
        if (TextUtils.isEmpty(address)) {
            mAddressView.setError(getString(R.string.error_field_required));
            mFocusView = mAddressView;
            valid = false;
        }

        // Check for valid phone number
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            mFocusView = mPhoneView;
            valid = false;
        } else if (!ValidationUtil.isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            mFocusView = mPhoneView;
            valid = false;
        }

        // Check for last name
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            mFocusView = mLastNameView;
            valid = false;
        }

        // Check for first name
        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            mFocusView = mFirstNameView;
            valid = false;
        }

        return valid;
    }

    /**
     * Create user on the web database
     * @param user User
     */
    private void createUserOnWeb(User user){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.WEB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CreateNewUserAPI createNewUserAPI = retrofit.create(CreateNewUserAPI.class);
        Call<ResponseBody> response = createNewUserAPI.createUser(user.getFirstName(), user.getLastName(), user.getPhone(), user.getAddress(),
                                        user.getUserName(), mPasswordView.getText().toString(), user.getUserID());
        response.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("Web User", response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Web User", t.getMessage());
            }
        });
    }

}
