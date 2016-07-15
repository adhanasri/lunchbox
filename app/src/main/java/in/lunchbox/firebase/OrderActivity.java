package in.lunchbox.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.CustomerInfo;
import Model.MenuItem;
import Model.Order;
import Model.User;
import Utils.CommonUtil;
import Utils.Constants;
import Utils.NumberUtil;
import in.lunchbox.firebase.websync.CreateNewOrderAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderActivity extends AppCompatActivity {

    TableLayout mTableLayout;
    Button mPlaceOrderButton;
    Button mCancelOrderButton;
    TextView mDeliveryEstimateView;
    EditText mDeliveryInstructionsView;
    Integer orderID;
    boolean showBackButton = true;

    // intent variables
    CustomerInfo customerInfo = null;
    Double totalAmount;
    List<MenuItem> orderItems;
    Double deliveryLatitude;
    Double deliveryLongitude;

    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSharedPreferences = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

        mTableLayout = (TableLayout) findViewById(R.id.orderSummary);
        mDeliveryEstimateView = (TextView) findViewById(R.id.deliveryEstimate);
        mDeliveryInstructionsView = (EditText) findViewById(R.id.deliveryInstructions);
        mPlaceOrderButton = (Button) findViewById(R.id.placeOrder);
        mCancelOrderButton = (Button) findViewById(R.id.cancelOrder);

        Intent intent = getIntent();
        showBackButton = intent.getBooleanExtra("showBackButton", true);
        if (showBackButton){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (null != intent.getSerializableExtra("customerInfo")){
            customerInfo = (CustomerInfo) intent.getSerializableExtra("customerInfo");
        }

        orderItems = (List<MenuItem>) intent.getSerializableExtra("orderItems");

        totalAmount = intent.getDoubleExtra("totalAmount", 0);

        // build table layout with order items
        CommonUtil.buildOrderItemsTable(mTableLayout, orderItems, this);

        deliveryLatitude = intent.getDoubleExtra("deliveryLatitude", 0);
        deliveryLongitude = intent.getDoubleExtra("deliveryLongitude", 0);
        mDeliveryEstimateView.setText(String.format(getString(R.string.deliveryEstimate_label), (CommonUtil.getApproxTravelTime(deliveryLatitude, deliveryLongitude) + 15)));


        mPlaceOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });

        mCancelOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder();
            }
        });

        getOrderIDFromSequence();
    }

    /**
     * Place the order
     */
    private void placeOrder(){

        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        String loggedInUser = (String)ref.getAuth().getProviderData().get("email");
        String userID = ref.getAuth().getUid();
        // create order
        Order order = new Order(loggedInUser, userID, orderItems, NumberUtil.roundTo2Decimals(totalAmount), deliveryLatitude, deliveryLongitude, Constants.REQUESTED, new Date(), new Date());
        order.setOrderID(orderID);
        order.setPriority(-1 * new Date().getTime());
        order.setInstructions(mDeliveryInstructionsView.getText().toString());
        // set customer info if not null(this is only populated when admin is creating an order for a customer)
        if (null != customerInfo){
            order.setCustomerInfo(customerInfo);
            if (null != customerInfo.getUserID()){
                order.setUserID(customerInfo.getUserID());
                order.setUserName(customerInfo.getEmail());
            }
        }

        Firebase userOrdersRef = new Firebase(Constants.FIREBASE_URL + "/orders");
        Firebase ordersRef = userOrdersRef.push();
        ordersRef.setValue(order);

        Toast.makeText(OrderActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
        // navigate to next screen
        navigateToNextActivity(ref.getAuth());
    }

    /**
     * Cancel the order
     */
    private void cancelOrder(){
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        Toast.makeText(OrderActivity.this, "Order cancelled", Toast.LENGTH_SHORT).show();
        // navigate to next screen
        navigateToNextActivity(ref.getAuth());
    }

    /**
     * Determine the next screen to go to
     * @param authData AuthDate
     */
    private void navigateToNextActivity(AuthData authData) {
        final String userID = authData.getUid();
        Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/" + "users");
        usersRef.orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren() != null){
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


    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.action_signout:
                Firebase ref = new Firebase(Constants.FIREBASE_URL);
                ref.unauth();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                break;
            case R.id.action_settings:

                break;
            case R.id.action_contact:
                Intent contactIntent = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(contactIntent);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer, menu);
        return true;
    }

    /**
     * Assign the order ID from sequence, append today's date to it
     */
    private void getOrderIDFromSequence(){
        final Firebase orderIDSeqRef = new Firebase(Constants.FIREBASE_URL + "/orderIDSequence");
        orderIDSeqRef.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer sequenceID = dataSnapshot.getValue(Integer.class);
                String currentDate = dateFormat.format(new Date());
                String generatedID = currentDate.substring(2) + (Integer.toString(sequenceID));
                orderID = Integer.parseInt(generatedID);
                orderIDSeqRef.setValue(sequenceID + 1);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Failed order ID read", firebaseError.toString());
            }
        });

    }

    /**
     * Create order on the web database
     * @param order Order
     */
    private void createOrderOnWeb(Order order){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.WEB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CreateNewOrderAPI createNewOrderAPI = retrofit.create(CreateNewOrderAPI.class);
        Call<Response> response = createNewOrderAPI.createOrder(order);
        response.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, Response<Response> response) {
                Log.i("Web Order", response.message());
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.e("Web Order", t.getMessage());
            }
        });
    }

}
