/**
 * Model class for one customer row in the table.
 * The getters must be public so PropertyValueFactory can read them.
 */
public class Customer {

    private final String name;
    private final String province;

    public Customer(String name, String province) {
        this.name = name;
        this.province = province;
    }

    public String getName() {
        return name;
    }

    public String getProvince() {
        return province;
    }

    @Override
    public String toString() {
        return name + " (" + province + ")";
    }
}