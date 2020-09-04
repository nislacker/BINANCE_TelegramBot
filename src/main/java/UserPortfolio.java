public class UserPortfolio {
    private int id;
    private int user_id;
    private int currency_id;
    private String address;
    private Double volume;

    public UserPortfolio() {
    }

    public UserPortfolio(int id, int user_id, int currency_id, String address, Double volume) {
        this.id = id;
        this.user_id = user_id;
        this.currency_id = currency_id;
        this.address = address;
        this.volume = volume;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }
}
