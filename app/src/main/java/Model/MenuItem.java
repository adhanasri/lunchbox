package Model;

import java.io.Serializable;

/**
 * Bean to store menu item data
 * Created by adhanas on 4/18/2016.
 */
public class MenuItem implements Serializable{

    String name;
    String description;
    Double price;
    Integer quantity;

    public MenuItem() {
    }

    public MenuItem(String name, String description, Double price, Integer quantity){
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }


}
