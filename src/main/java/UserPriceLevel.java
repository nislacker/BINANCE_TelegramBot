public class UserPriceLevel {
    private Long id;
    private Long user_id;
    private Long currency_id;
    private double price_level;
    private boolean is_notified;
    private boolean is_higher_level;

    public UserPriceLevel() {
    }

    public UserPriceLevel(Long user_id, Long currency_id, double price_level, boolean is_notified, boolean is_higher_level) {
        this.user_id = user_id;
        this.currency_id = currency_id;
        this.price_level = price_level;
        this.is_notified = is_notified;
        this.is_higher_level = is_higher_level;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Long getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(Long currency_id) {
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
