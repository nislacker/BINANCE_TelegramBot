public class User {

    private int id;
    private long chat_id;

    public User(int id, long chat_id) {
        super(); // ???
        this.id = id;
        this.chat_id = chat_id;
    }
    @Override
    public String toString() {
        return "User [id=" + id + ", chat_id=" + chat_id + "]";
    }

}
