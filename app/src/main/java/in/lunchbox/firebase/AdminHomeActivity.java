package in.lunchbox.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;

import Model.AdminListItem;
import Model.Order;
import Utils.CommonUtil;
import Utils.Constants;

public class AdminHomeActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    Firebase ref;

    ListViewCompat mOrderListView;

    AdminListAdapter listAdapter;
    ArrayList<AdminListItem> ordersList;

    SharedPreferences mSharedPreferences;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (null != fab){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AdminCustomerActivity.class);
                    startActivity(intent);
                }
            });
        }

        ref = new Firebase(Constants.FIREBASE_URL);
        mSharedPreferences = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

        mOrderListView = (ListViewCompat) findViewById(R.id.orderList);

        ordersList = new ArrayList<>();
        listAdapter = new AdminListAdapter(this, android.R.layout.simple_list_item_2, ordersList);
        mOrderListView.setAdapter(listAdapter);

        updateOrdersList();

        mOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openOrderActivity(position);
            }
        });

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

        createNotifications();
    }

    /**
     * Method to navigate admin to the order screen for a particular order selected
     * @param position int
     */
    private void openOrderActivity(int position){
        if (!ordersList.get(position).getOrderFBID().isEmpty()){
            Intent orderIntent = new Intent(getApplicationContext(), AdminOrderActivity.class);
            orderIntent.putExtra("orderFBID", ordersList.get(position).getOrderFBID());
            orderIntent.putExtra("userID", ordersList.get(position).getUserID());
            startActivity(orderIntent);
        }
    }

    /**
     * Method to read the order list from firebase to populate the admin home screen
     */
    private void updateOrdersList() {

        Firebase orderRef = new Firebase(Constants.FIREBASE_URL + "/orders");
        orderRef.orderByChild("priority").limitToFirst(50).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ordersList.clear();
                Iterable<DataSnapshot> childrenSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot child : childrenSnapshots) {
                    Order order = child.getValue(Order.class);
                    AdminListItem item = new AdminListItem(Integer.toString(order.getOrderID()), CommonUtil.getMessage(order), child.getKey(), order.getUserID(), order.getUserName());
                    ordersList.add(item);
                }
                addBufferListItems();
                listAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * Method to check for any new orders created and generate notifications for the admin
     */
    private void createNotifications() {
        final Intent intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Firebase orderRef = new Firebase(Constants.FIREBASE_URL + "/orders");
        orderRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Order order = dataSnapshot.getValue(Order.class);
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("New Order!")
                        .setContentText("Box ID: " + order.getOrderID())
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert).build();
                notificationManager.notify(1, notification);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin_home, menu);

        // Set up search view
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != getCurrentFocus()){
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }

                ArrayList<AdminListItem> filteredList = new ArrayList<>();
                for (AdminListItem item : ordersList){
                    if (item.getOrderID().contains(query)){
                        filteredList.add(item);
                    }
                }
                if (filteredList.size() == 0){
                    Toast.makeText(AdminHomeActivity.this, "No Search results", Toast.LENGTH_SHORT).show();
                } else {
                    ordersList.clear();
                    ordersList.addAll(filteredList);
                    addBufferListItems();
                    listAdapter.notifyDataSetChanged();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    /**
     * Method to add dummy list items to allow the admin to scroll items above the android nav bar
     */
    private void addBufferListItems(){
        AdminListItem item = new AdminListItem("0", "", "", "", "");
        ordersList.add(item);
        ordersList.add(item);
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
            case R.id.action_refresh:
                updateOrdersList();
                break;
            case R.id.action_menu:
                Intent menuIntent = new Intent(getApplicationContext(), AdminMenuActivity.class);
                startActivity(menuIntent);
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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
