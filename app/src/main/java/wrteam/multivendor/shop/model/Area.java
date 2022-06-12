package wrteam.multivendor.shop.model;

public class Area {
    String id,city_id,pincode_id,name,minimum_free_delivery_order_amount,delivery_charges,city_name,pincode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity_id() {
        return city_id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public String getPincode_id() {
        return pincode_id;
    }

    public void setPincode_id(String pincode_id) {
        this.pincode_id = pincode_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMinimum_free_delivery_order_amount() {
        return minimum_free_delivery_order_amount;
    }

    public void setMinimum_free_delivery_order_amount(String minimum_free_delivery_order_amount) {
        this.minimum_free_delivery_order_amount = minimum_free_delivery_order_amount;
    }

    public String getDelivery_charges() {
        return delivery_charges;
    }

    public void setDelivery_charges(String delivery_charges) {
        this.delivery_charges = delivery_charges;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
}
