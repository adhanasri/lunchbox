package Model;

/**
 * Created by adhanas on 4/10/2016.
 */
public class AdminListItem {

    String orderID;
    String orderDetails;
    String orderFBID;
    String userID;
    String userName;

    public AdminListItem(String orderID, String orderDetails, String orderFBID, String userID, String userName) {
        this.orderID = orderID;
        this.orderDetails = orderDetails;
        this.orderFBID = orderFBID;
        this.userID = userID;
        this.userName = userName;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getOrderFBID() {
        return orderFBID;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }
}
