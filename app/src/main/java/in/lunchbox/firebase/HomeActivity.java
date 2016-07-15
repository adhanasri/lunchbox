package in.lunchbox.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.HistoryItem;
import Model.Order;
import Model.User;
import Utils.CommonUtil;
import Utils.Constants;

public class HomeActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    List<HistoryItem> historyItems;
    RecyclerView mHistoryListView;
    HistoryItemRVAdapter historyItemRVAdapter;
    TextView mWelcomeTextView;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Shared Preferences
    SharedPreferences mSharedPreferences;

    // Firebase reference
    Firebase ref;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (null != fab){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                    startActivity(intent);
                }
            });
        }

        ref = new Firebase(Constants.FIREBASE_URL);
        mSharedPreferences = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

        userID = ref.getAuth().getUid();

        mHistoryListView = (RecyclerView) findViewById(R.id.historyListView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mHistoryListView.setLayoutManager(layoutManager);

        getWelcomeText();

        initializeData();

        if (ref.getAuth().getProvider().equals("google")){
            // Setup the Google API object
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    /**
     * Generate the welcome text for home screen
     */
    private void getWelcomeText() {
        mWelcomeTextView = (TextView) findViewById(R.id.welcomeText);

        if (ref.getAuth().getProvider().equals("google")){
            String name = (String)ref.getAuth().getProviderData().get("displayName");
            name = name.contains(" ") ? name.split(" ")[0] : name;// split to get first name
            mWelcomeTextView.setText(String.format(getString(R.string.welcome_greeting), name));
        } else {
            Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/users");
            usersRef.orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                    User user = userSnapshot.getValue(User.class);
                    mWelcomeTextView.setText(String.format(getString(R.string.welcome_greeting), user.getFirstName()));
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        initializeData();
        super.onResume();
    }

    /**
     * Generate order history items for home screen
     */
    private void initializeData() {

        Firebase orderRef = new Firebase(Constants.FIREBASE_URL + "/orders");
        orderRef.orderByChild("userID").equalTo(userID).limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyItems = new ArrayList<>();
                Iterable<DataSnapshot> childrenSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot child : childrenSnapshots) {
                    Order order = child.getValue(Order.class);
                    historyItems.add(new HistoryItem(Integer.toString(order.getOrderID()), order.getStatus(), CommonUtil.getMessage(order), order.getMenuItems(), child.getKey()));
                }
                if (historyItems.size() > 0) {
                    Collections.reverse(historyItems);
                    historyItemRVAdapter = new HistoryItemRVAdapter(historyItems);
                    mHistoryListView.setAdapter(historyItemRVAdapter);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer_home, menu);
        if (!ref.getAuth().getProvider().equals("google")){
            menu.removeItem(R.id.action_google_signout);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mGoogleApiClient){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signout:
                ref.unauth();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                break;
            case R.id.action_google_signout:
                ref.unauth();
                SharedPreferences.Editor shEditor1 = mSharedPreferences.edit();
                shEditor1.clear();
                shEditor1.apply();
                mGoogleApiClient.clearDefaultAccountAndReconnect();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                /*Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);*/
                break;
            case R.id.action_contact:
                Intent contactIntent = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(contactIntent);
                break;
            case android.R.id.home:
                // do nothing
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
