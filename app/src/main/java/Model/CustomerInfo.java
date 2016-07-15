package Model;

import java.io.Serializable;

/**
 * Bean to store customer info
 * Created by adhanas on 4/18/2016.
 */
public class CustomerInfo implements Serializable{

    String firstName;
    String lastName;
    String email;
    Long phone;
    String userID;

    public CustomerInfo() {
    }

    public CustomerInfo(String firstName, String lastName, String email, Long phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Long getPhone() {
        return phone;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}
