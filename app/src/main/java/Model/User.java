package Model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Bean to store user data
 * Created by adhanas on 4/18/2016.
 */
public class User implements Serializable{

    String firstName;
    String lastName;
    String address;
    Long phone;
    Date dateCreated;
    Date lastUpdated;
    String status;
    String userName;
    String userID;
    String type = "customer";
    List<Order> orders;

    public User() {
    }

    public User(String firstName, String lastName, Long phone, String address, Date dateCreated, Date lastUpdated, String status, String userName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.dateCreated = dateCreated;
        this.lastUpdated = lastUpdated;
        this.status = status;
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public Long getPhone() {
        return phone;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getStatus() {
        return status;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return type;
    }

}
