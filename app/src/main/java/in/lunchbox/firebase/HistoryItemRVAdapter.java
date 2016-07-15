package in.lunchbox.firebase;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import Model.HistoryItem;
import Model.MenuItem;
import Model.Order;
import Utils.CommonUtil;
import Utils.Constants;
import Utils.NumberUtil;

/**
 * Adapter to maintain the history items view and actions shown on the home screen
 * Created by adhanas on 4/19/2016.
 */
public class HistoryItemRVAdapter extends RecyclerView.Adapter<HistoryItemViewHolder> {

    public List<HistoryItem> historyItems;
    String boxID;
    // Firebase reference URL
    Firebase ref = new Firebase(Constants.FIREBASE_URL);
    // Logged in user ID
    String userID = ref.getAuth().getUid();
    Context context;

    public HistoryItemRVAdapter(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @Override
    public HistoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.historyitem, null);
        return new HistoryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HistoryItemViewHolder holder, int position) {

        holder.historyItemTitleView.setText(String.format(holder.historyItemTitleView.getContext().getString(R.string.boxID_label), historyItems.get(position).getTitle()));
        holder.historyItemStatusView.setText(String.format(holder.historyItemStatusView.getContext().getString(R.string.status_label), historyItems.get(position).getStatus()));
        holder.historyItemDeliveryInfoView.setText(historyItems.get(position).getDeliveryInfo());
        if (historyItems.get(position).getStatus().equals(Constants.OPEN)){
            /*holder.orderButton.setVisibility(View.VISIBLE);
            holder.cancelOrderButton.setVisibility(View.VISIBLE);*/
        } else if (historyItems.get(position).getStatus().equals(Constants.COMPLETED)){
           /* holder.orderAgainButton.setVisibility(View.VISIBLE);*/
        } else if (historyItems.get(position).getStatus().equals(Constants.IN_TRANSIT)){
            //holder.trackDeliveryButton.setVisibility(View.VISIBLE);
        }

        /*holder.trackDeliveryButton.setTag(historyItems.get(position).getOrderFBID());
        holder.trackDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Button", "clicked");
            }
        });

        holder.cancelOrderButton.setTag(historyItems.get(position).getOrderFBID());
        holder.cancelOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder(v);
            }
        });

        holder.orderButton.setTag(historyItems.get(position).getOrderFBID());
        holder.orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder(v);
            }
        });

        holder.orderAgainButton.setTag(historyItems.get(position).getOrderFBID());
        holder.orderAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderAgain(v);
            }
        });*/

        holder.historyItemCardView.setTag(historyItems.get(position).getOrderFBID());
        holder.historyItemCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUp(v);
            }
        });
    }

    /**
     * Method to cancel the order
     * @param v View
     */
    private void cancelOrder(View v){
        boxID = v.getTag().toString();
        Firebase orderRef = new Firebase(Constants.FIREBASE_URL + "/orders/" + userID + "/" + boxID);
        orderRef.child("status").setValue("cancelled");
    }

    /**
     * Method to place an order
      * @param v View
     */
    private void placeOrder(View v){
        context = v.getContext();
        boxID = v.getTag().toString();
        Firebase orderRef = new Firebase(Constants.FIREBASE_URL + "/orders/" + boxID);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);
                List<MenuItem> orderItems = order.getMenuItems();
                Double totalAmount = order.getTotalAmount();
                order.setPriority(-1 * new Date().getTime());

                // navigate to order activity
                gotoOrderActivity(orderItems, totalAmount, order.getDeliveryLatitude(), order.getDeliveryLongitude());

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * Method to place the same order again
     * @param v View
     */
    private void orderAgain(View v){
        context = v.getContext();
        boxID = v.getTag().toString();
        Firebase orderRef = new Firebase(Constants.FIREBASE_URL + "/orders/" + boxID);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);
                List<MenuItem> orderItems = order.getMenuItems();
                Double totalAmount = order.getTotalAmount();

                // navigate to order activity
                gotoOrderActivity(orderItems, totalAmount, order.getDeliveryLatitude(), order.getDeliveryLongitude());

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("Error", firebaseError.toString());
            }
        });
    }

    /**
     * Method to navigate to Order screen
     * @param orderItems List<MenuItem>
     * @param totalAmount Double
     * @param latitude Double
     * @param longitude Double
     */
    private void gotoOrderActivity(List<MenuItem> orderItems, Double totalAmount, Double latitude, Double longitude){
        Intent intent = new Intent(context, OrderActivity.class);
        intent.putExtra("orderItems", (Serializable) orderItems);
        intent.putExtra("totalAmount", totalAmount);
        intent.putExtra("deliveryLatitude", latitude);
        intent.putExtra("deliveryLongitude", longitude);
        intent.putExtra("showBackButton", false);
        context.startActivity(intent);
    }

    /**
     * Method to show pop up with the order information
     * @param v View
     */
    private void showPopUp(final View v){
        context = v.getContext();
        Log.i("Card", "clicked");
        boxID = v.getTag().toString();
        Log.i("Box ID", boxID);

        final View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.order_popup, null);

        Firebase orderRef = new Firebase(Constants.FIREBASE_URL + "/orders/" + boxID);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);
                List<MenuItem> orderItems = order.getMenuItems();

                TableLayout tableLayout = (TableLayout) popupView.findViewById(R.id.orderSummary);
                TextView popupHeaderView = (TextView) popupView.findViewById(R.id.reviewText);
                popupHeaderView.setText(String.format(context.getString(R.string.boxID_label), order.getOrderID()));

                // build table layout with order items
                CommonUtil.buildOrderItemsTable(tableLayout, orderItems, popupView.getContext());

                // build pop up dialog
                popupView.setTag(boxID);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                dialogBuilder.setView(popupView)
                .setPositiveButton("Re-Order", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        orderAgain(popupView);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog dialog = dialogBuilder.create();

                /*ImageButton dismissButton = (ImageButton) popupView.findViewById(R.id.dismiss);
                dismissButton.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/

                // show popup
                dialog.show();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }
}
