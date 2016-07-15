package in.lunchbox.firebase.websync;

import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import Model.CustomerInfo;
import Model.MenuItem;
import Model.Order;
import Utils.JSONParser;

/**
 * Create new order on the web database
 * Created by adhanas on 4/24/2016.
 */
public class CreateNewOrder extends AsyncTask<Order, Void, String>{

    private static final String TAG = CreateNewOrder.class.getSimpleName();

    JSONParser jsonParser = new JSONParser();

    // url to create new order
    private static String url_create_order = "http://api.androidhive.info/android_connect/create_product.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    @Override
    protected String doInBackground(Order... orders) {
        // initialize order
        Order order = orders[0];
        Integer orderID = order.getOrderID();
        String userName = order.getUserName();
        String userID = order.getUserID();
        Double totalAmount = order.getTotalAmount();
        Double deliveryLatitude = order.getDeliveryLatitude();
        Double deliveryLongitude = order.getDeliveryLongitude();
        String status = order.getStatus();
        Long priority = order.getPriority();
        String instructions = order.getInstructions() != null ? order.getInstructions() : "";
        String reason = order.getReason() != null ? order.getReason() : "";
        List<MenuItem> menuItems = order.getMenuItems();
        CustomerInfo customerInfo = order.getCustomerInfo() != null ? order.getCustomerInfo() : null;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("orderID", Integer.toString(orderID)));


        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }


}
