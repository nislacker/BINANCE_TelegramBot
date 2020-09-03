public class UserPriceLevel {
    private int id;
    private int user_id;
    private int currency_id;
    private double price_level;
    private boolean is_notified;
    private boolean is_higher_level;

    public UserPriceLevel() {
    }

    public UserPriceLevel(int user_id, int currency_id, double price_level, boolean is_notified, boolean is_higher_level) {
        this.user_id = user_id;
        this.currency_id = currency_id;
        this.price_level = price_level;
        this.is_notified = is_notified;
        this.is_higher_level = is_higher_level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }

    public double getPrice_level() {
        return price_level;
    }

    public void setPrice_level(double price_level) {
        this.price_level = price_level;
    }

    public boolean isIs_notified() {
        return is_notified;
    }

    public void setIs_notified(boolean is_notified) {
        this.is_notified = is_notified;
    }

    public boolean isIs_higher_level() {
        return is_higher_level;
    }

    public void setIs_higher_level(boolean is_higher_level) {
        this.is_higher_level = is_higher_level;
    }
}
