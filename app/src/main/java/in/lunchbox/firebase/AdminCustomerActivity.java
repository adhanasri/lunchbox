package in.lunchbox.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import Model.CustomerInfo;
import Model.User;
import Utils.CommonUtil;
import Utils.Constants;
import Utils.ValidationUtil;

public class AdminCustomerActivity extends AppCompatActivity {

    EditText mFirstNameView;
    EditText mLastNameView;
    EditText mPhoneView;
    EditText mEmailView;
    Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFirstNameView = (EditText) findViewById(R.id.customerFirstName);
        mLastNameView = (EditText) findViewById(R.id.customerLastName);
        mPhoneView = (EditText) findViewById(R.id.customerPhone);
        mEmailView = (EditText) findViewById(R.id.customerEmail);

        // save customer info button
        mSaveButton = (Button) findViewById(R.id.saveCustomerInfo);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomerInfo();
            }
        });

    }

    /**
     * Method to validate the email and phone number entered on the screen
     * @param email String
     * @param phone String
     * @return boolean
     */
    private boolean validateFields(String email, String phone) {
        boolean valid = true;
        if (email.isEmpty() && phone.isEmpty()){
            Toast.makeText(AdminCustomerActivity.this, "Either Email or Phone is mandatory", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!email.isEmpty() && !ValidationUtil.isEmailValid(email)){
            Toast.makeText(AdminCustomerActivity.this, "This email address is invalid", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (!phone.isEmpty() && !ValidationUtil.isPhoneValid(phone)){
            Toast.makeText(AdminCustomerActivity.this, "This phone number is invalid", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    /**
     * Method to save customer info entered on the screen
     */
    private void saveCustomerInfo(){
        final String email = mEmailView.getText().toString();
        final String phone = mPhoneView.getText().toString();

        boolean valid = validateFields(email, phone);

        if (valid){
            Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/" + "users");
            usersRef.orderByChild("userName").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                    CustomerInfo info = new CustomerInfo(mFirstNameView.getText().toString(), mLastNameView.getText().toString(),
                            email, phone.isEmpty() ? 0 : CommonUtil.get10DigitPhone(phone));
                    if (dataSnapshot.getValue() != null && dataSnapshot.getChildren() != null){
                        // user exists in the system, set user id
                        User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                        info.setUserID(user.getUserID());
                    }
                    intent.putExtra("customerInfo", info);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

}
