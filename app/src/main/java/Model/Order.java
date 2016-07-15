package Model;

import java.util.Date;
import java.util.List;

/**
 * Bean to store order data
 * Created by adhanas on 4/18/2016.
 */
public class Order {

    Integer orderID;
    String userName;
    String userID;
    List<MenuItem> menuItems;
    Double totalAmount;
    Double deliveryLatitude;
    Double deliveryLongitude;
    String status;
    Date dateCreated;
    Date lastUpdated;
    Long priority;
    CustomerInfo customerInfo;
    String instructions;
    String reason;

    public Order() {
    }

    public Order(String userName, String userID, List<MenuItem> menuItems, Double totalAmount, Double deliveryLatitude, Double deliveryLongitude, String status, Date dateCreated, Date lastUpdated) {
        this.userName = userName;
        this.userID = userID;
        this.menuItems = menuItems;
        this.totalAmount = totalAmount;
        this.deliveryLatitude = deliveryLatitude;
        this.deliveryLongitude = deliveryLongitude;
        this.status = status;
        this.dateCreated = dateCreated;
        this.lastUpdated = lastUpdated;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }

    public Integer getOrderID() {
        return orderID;
    }

    public String getUserName() {
        return userName;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getUserID() {
        return userID;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
