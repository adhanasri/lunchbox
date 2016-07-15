package Model;


/**
 * Bean to store menu item info
 * Created by adhanas on 6/17/2016.
 */
public class AdminMenuItem{

    String name;
    String description;
    Double price;
    String status;
    String type;

    String id;

    public AdminMenuItem() {
    }

    public AdminMenuItem(String name, String description, Double price, String status, String type) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.type = type;
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

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
