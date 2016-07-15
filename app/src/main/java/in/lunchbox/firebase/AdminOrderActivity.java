package in.lunchbox.firebase;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Date;
import java.util.List;

import Model.CustomerInfo;
import Model.MenuItem;
import Model.Order;
import Model.User;
import Utils.CommonUtil;
import Utils.Constants;
import Utils.NumberUtil;

public class AdminOrderActivity extends AppCompatActivity {

    String orderFBID;
    String userID;
    String orderStatus = "";

    Context context;
    Firebase orderRef;

    TableLayout mTableLayout;
    TextView mAdminText;
    Button mChangeStatusButton;
    Button mCancelOrderButton;

    TextView mCustomerNameView;
    TextView mCustomerPhoneView;

    TextView mDeliveryInstructionsView;

    EditText mCancelReasonView;

    ActionBar actionBar;

    Firebase ref;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(null != getSupportActionBar()){
            actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ref = new Firebase(Constants.FIREBASE_URL);
        mSharedPreferences = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

        context = this;

        Intent intent = getIntent();
        orderFBID = intent.getStringExtra("orderFBID");
        userID = intent.getStringExtra("userID");

        mTableLayout = (TableLayout) findViewById(R.id.adminOrderSummary);
        mAdminText = (TextView) findViewById(R.id.adminReviewText);
        mChangeStatusButton = (Button) findViewById(R.id.adminProgressOrder);
        mCancelOrderButton = (Button) findViewById(R.id.adminCancelOrder);

        mCustomerNameView = (TextView) findViewById(R.id.customerName);
        mCustomerPhoneView = (TextView) findViewById(R.id.customerPhone);

        mDeliveryInstructionsView = (TextView) findViewById(R.id.deliveryInstructionsView);

        orderRef = new Firebase(Constants.FIREBASE_URL + "/orders/" + orderFBID);
        getOrderInfo();

        mChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeOrderStatus();
            }
        });

        mCancelOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCancelDialog();
            }
        });

        mCustomerPhoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callCustomer();
            }
        });

    }

    /**
     * Method to progress the order status
     */
    private void changeOrderStatus(){
        if (!orderStatus.isEmpty()){
            switch (orderStatus){
                case Constants.OPEN:
                    orderRef.child("status").setValue(Constants.REQUESTED);
                    orderRef.child("lastUpdated").setValue(new Date());
                    break;
                case Constants.REQUESTED:
                    orderRef.child("status").setValue(Constants.IN_PROGRESS);
                    orderRef.child("lastUpdated").setValue(new Date());
                    break;
                case Constants.IN_PROGRESS:
                    orderRef.child("status").setValue(Constants.IN_TRANSIT);
                    orderRef.child("lastUpdated").setValue(new Date());
                    break;
                case Constants.IN_TRANSIT:
                    orderRef.child("status").setValue(Constants.COMPLETED);
                    orderRef.child("lastUpdated").setValue(new Date());
                    mChangeStatusButton.setVisibility(View.INVISIBLE);
                    mCancelOrderButton.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    /**
     * Method to invoke a cancel order confirmation dialog
     */
    private void openCancelDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.order_cancel_reason, null);

        dialogBuilder.setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel the order?")
                .setView(view)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelOrder();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = dialogBuilder.create();
        mCancelReasonView = (EditText) view.findViewById(R.id.cancelReason);
        dialog.show();
    }

    /**
     * Method to cancel the order
     */
    private void cancelOrder(){
        orderRef.child("status").setValue(Constants.CANCELLED);
        orderRef.child("reason").setValue(mCancelReasonView.getText().toString());
        mChangeStatusButton.setVisibility(View.INVISIBLE);
        mCancelOrderButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Method to call the customer
     */
    private void callCustomer(){
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + mCustomerPhoneView.getText().toString().substring(7)));
        startActivity(phoneIntent);
    }

    /**
     * Method to get the customer information from firebase
     * @param userID String
     */
    private void getCustomerInfoFromFirebase(final String userID) {
        Firebase usersRef = new Firebase(Constants.FIREBASE_URL + "/" + "users");
        usersRef.orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getChildren() != null){
                    User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    mCustomerNameView.setText(String.format(getString(R.string.admin_order_customer_name), user.getFirstName(), user.getLastName()));
                    mCustomerPhoneView.setText(String.format(getString(R.string.admin_order_customer_phone), user.getPhone()));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Error finding userID", userID);
                Log.e("Error message", firebaseError.getMessage());
            }
        });
    }

    /**
     * Method to get the order information
     */
    private void getOrderInfo(){
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);
                List<MenuItem> orderItems = order.getMenuItems();

                // set screen title
                if (null != actionBar && null != actionBar.getTitle() && !actionBar.getTitle().toString().contains(Integer.toString(order.getOrderID()))){
                    actionBar.setTitle(actionBar.getTitle().toString().concat(" ").concat(Integer.toString(order.getOrderID())));
                }

                mTableLayout.removeAllViews();

                orderStatus = order.getStatus();
                mAdminText.setText(String.format(context.getString(R.string.status_label), orderStatus));

                manageScreenView();

                if (null != order.getCustomerInfo()){
                    CustomerInfo info = order.getCustomerInfo();
                    mCustomerNameView.setText(String.format(getString(R.string.admin_order_customer_name), info.getFirstName(), info.getLastName()));
                    mCustomerPhoneView.setText(String.format(getString(R.string.admin_order_customer_phone), info.getPhone()));
                } else {
                    getCustomerInfoFromFirebase(order.getUserID());
                }

                if (null != order.getInstructions() && !order.getInstructions().isEmpty()){
                    mDeliveryInstructionsView.setText(order.getInstructions());
                } else {
                    mDeliveryInstructionsView.setVisibility(View.INVISIBLE);
                }

                // build table layout with order items
                CommonUtil.buildOrderItemsTable(mTableLayout, orderItems, context);

                // show reason for cancellation
                if (orderStatus.equals(Constants.CANCELLED) && null != order.getReason()){
                    showCancelReason(order.getReason());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * Method to add reason text to the order table layout
     * @param reasonText String
     */
    private void showCancelReason(String reasonText){
        // adding buffer view to space out the table from the reason
        TextView buffer = new TextView(context);
        buffer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, R.dimen.text_margin));
        buffer.setText("                             ");
        mTableLayout.addView(buffer);

        // add reason view
        TextView reason = new TextView(context);
        reason.setText(String.format(getString(R.string.reason),reasonText));
        reason.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        reason.setTextSize(16);

        mTableLayout.addView(reason);
    }

    /**
     * Method to hide action buttons when order is completed or cancelled and set status color
     */
    private void manageScreenView(){
        if (orderStatus.equals(Constants.COMPLETED) || orderStatus.equals(Constants.CANCELLED)){
            mChangeStatusButton.setVisibility(View.INVISIBLE);
            mCancelOrderButton.setVisibility(View.INVISIBLE);

            // set color to accent when order is cancelled and to primary when order is completed
            if (orderStatus.equals(Constants.CANCELLED)){
                mAdminText.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            } else {
                mAdminText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signout:
                ref.unauth();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                break;
            case R.id.action_settings:

                break;
            case android.R.id.home:
                Intent locationIntent = new Intent(getApplicationContext(), AdminHomeActivity.class);
                startActivity(locationIntent);
        }
        return true;
    }

}
