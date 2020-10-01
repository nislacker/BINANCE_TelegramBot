public class UserPortfolio {
    private Long id;
    private Long user_id;
    private Long currency_id;
    private String address;
    private Double balance;

    public UserPortfolio() {
    }

    public UserPortfolio(Long id, Long user_id, Long currency_id, String address, Double balance) {
        this.id = id;
        this.user_id = user_id;
        this.currency_id = currency_id;
        this.address = address;
        this.balance = balance;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
