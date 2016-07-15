package Utils;

import android.content.Context;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import Model.MenuItem;
import Model.Order;
import in.lunchbox.firebase.R;

/**
 * Util class to contain all the common methods
 * Created by adhanas on 4/19/2016.
 */
public class CommonUtil {

    public static Integer generateRandomOrderID(){
        Integer returnOrderID;
        Random random = new Random(System.nanoTime());
        returnOrderID = random.nextInt(9999);
        return returnOrderID;
    }

    public static String getTimeStamp(Date date) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.ENGLISH);
        return sdfDate.format(date);
    }

    /**
     * Calculate approx. travel time in minutes assuming the speed to be 18km/hr
     * @param latitude double
     * @param longitude double
     * @return Integer
     */
    public static Integer getApproxTravelTime(double latitude, double longitude){
        android.location.Location storeLocation = new android.location.Location("");
        storeLocation.setLatitude(Constants.storeLatitude);
        storeLocation.setLongitude(Constants.storeLongitude);
        android.location.Location orderLocation = new android.location.Location("");
        orderLocation.setLatitude(latitude);
        orderLocation.setLongitude(longitude);
        Float distance = storeLocation.distanceTo(orderLocation);
        Float time = ((distance / 18000) * 60) + 5;
        time = time > 10 ? time : 10;
        return time.intValue();
    }


    public static Long get10DigitPhone(String phone){
        if (phone.contains("+")){
            phone = phone.substring(3);
        }
        return Long.parseLong(phone);
    }

    /**
     * Generate the custom message for each order item
     * @param order Order
     * @return String
     */
    public static String getMessage(Order order){
        String message = "";
        switch (order.getStatus()){
            case Constants.REQUESTED:
                message = "Order placed at " + CommonUtil.getTimeStamp(order.getLastUpdated());
                break;
            case Constants.IN_PROGRESS:
                message = "Delivery estimate - " + (CommonUtil.getApproxTravelTime(order.getDeliveryLatitude(), order.getDeliveryLongitude()) + 15) + " mins.";
                break;
            case Constants.IN_TRANSIT:
                message = "Delivery estimate - " + (CommonUtil.getApproxTravelTime(order.getDeliveryLatitude(), order.getDeliveryLongitude())) + " mins.";
                break;
            case Constants.COMPLETED:
                message = "Order delivered at " + CommonUtil.getTimeStamp(order.getLastUpdated());
                break;
            case Constants.CANCELLED:
                message = "Order cancelled at " + CommonUtil.getTimeStamp(order.getLastUpdated());
                break;
            case Constants.OPEN:
                message = "Please click 'Order' to complete.";
                break;
        }
        return message;
    }

    /**
     * Util method to build the table layout to show the order items
     * @param mTableLayout TableLayout
     * @param orderItems List<MenuItem>
     * @param context Context
     */
    public static void buildOrderItemsTable(TableLayout mTableLayout, List<MenuItem> orderItems, Context context){
        Double totalAmount = NumberUtil.roundTo2Decimals(Double.valueOf("0.00"));

        for (MenuItem menuItem : orderItems){
            Log.i("Menu Items", menuItem.getName());
            TableRow tableRow = new TableRow(context);

            TextView description = new TextView(context);
            description.setText(String.format(context.getString(R.string.menuitem_table_text), menuItem.getQuantity(), menuItem.getName()));
            description.setTextSize(16);
            tableRow.addView(description, 0);

            TextView price = new TextView(context);
            price.setText(String.format(context.getString(R.string.rupee_symbol), Double.toString(NumberUtil.roundTo2Decimals(menuItem.getPrice() * menuItem.getQuantity()))));
            price.setTextSize(16);
            tableRow.addView(price, 1);

            mTableLayout.addView(tableRow);

            totalAmount = totalAmount + menuItem.getQuantity() * menuItem.getPrice();

        }

        // Add divider row
        TableRow tableRowDiv = new TableRow(context);

        TextView div1 = new TextView(context);
        div1.setText("----------------");
        div1.setTextSize(16);
        tableRowDiv.addView(div1, 0);

        TextView div2 = new TextView(context);
        div2.setText("----------------");
        div2.setTextSize(16);
        tableRowDiv.addView(div2, 1);

        mTableLayout.addView(tableRowDiv);

        // Add total amount row
        TableRow tableRowTotal = new TableRow(context);

        TextView total = new TextView(context);
        total.setText(context.getString(R.string.total_label));
        total.setTextSize(16);
        tableRowTotal.addView(total, 0);

        TextView totalPrice = new TextView(context);
        totalPrice.setText(String.format(context.getString(R.string.rupee_symbol), Double.toString(NumberUtil.roundTo2Decimals(totalAmount))));
        totalPrice.setTextSize(16);
        tableRowTotal.addView(totalPrice, 1);

        mTableLayout.addView(tableRowTotal);
    }

}
