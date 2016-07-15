package Model;

import java.util.List;

/**
 * Bean to store order data to be shown on the home screen
 * Created by adhanas on 4/18/2016.
 */
public class HistoryItem {

    String title;
    String status;
    String deliveryInfo;
    String orderFBID;
    List<MenuItem> orderItems;

    public HistoryItem() {
    }

    public HistoryItem(String title, String status, String deliveryInfo, List<MenuItem> orderItems, String orderFBID) {
        this.title = title;
        this.status = status;
        this.deliveryInfo = deliveryInfo;
        this.orderItems = orderItems;
        this.orderFBID = orderFBID;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getDeliveryInfo() {
        return deliveryInfo;
    }

    public List<MenuItem> getOrderItems() {
        return orderItems;
    }

    public String getOrderFBID() {
        return orderFBID;
    }

}
