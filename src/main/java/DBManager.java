import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    // 127.0.0.1
    private static String url = "jdbc:mysql://localhost/crypto?useUnicode=true&serverTimezone=UTC";
    private static String userName = "root";
    private static String password = "";

    // -- Reset MySQL Autoincrement column --
    //
    // ALTER TABLE table_name AUTO_INCREMENT = 1;
    // TRUNCATE TABLE table_name;

    // 100%
    public static void truncateTable(String tableName) {
        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();

            String sql = "TRUNCATE TABLE `" + tableName + "`";
            stat.executeUpdate(sql);
            System.out.println("Table \"" + tableName + "\" truncated!");

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
        }
    }

    // 100%
    public static void insertNewUser(long chat_id) {

        if (isNewUser(chat_id)) {

            try (Connection con = DriverManager.getConnection(url, userName, password)) {
                System.out.println("Connection done!");

                Statement stat = con.createStatement();

                String sql = "INSERT INTO `users` VALUES (null, " + chat_id + ")";
                stat.executeUpdate(sql);
                System.out.println("sql Insert done!");
/*
            String sql ="SELECT * FROM users";

            ResultSet res = stat.executeQuery(sql);

            List<User> users=new ArrayList<>();

            while(res.next()) {
                User user = new User(res.getInt("id"), res.getLong("chat_id"));
                users.add(user);
            }

            System.out.println(users);
            */

            } catch (SQLException ex) {
                System.out.println("Something wrong with connection to DB...");
                ex.printStackTrace();
            }
        }
    }

    // 100%
    public static boolean isNewUser(long chat_id) {

        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT * FROM `users` WHERE `chat_id` = " + chat_id;

            ResultSet res = stat.executeQuery(sql);
            List<User> users = new ArrayList<>();

            while (res.next()) {
                return false;
//                User user = new User(res.getInt("id"), res.getLong("chat_id"));
//                users.add(user);
            }

            return true;

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return false;
        }
    }

    // 100%
    public static ArrayList<String> getUserCoins(long chat_id) {

        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            // сложный запрос, возможно лучше упростить
            String sql = "SELECT currency FROM currencies WHERE id IN (SELECT currency_id FROM users_currencies WHERE user_id = (SELECT id FROM `users` WHERE chat_id = " + chat_id + "))";

            ResultSet res = stat.executeQuery(sql);
            ArrayList<String> userCoins = new ArrayList<>();

            while (res.next()) {
                userCoins.add(res.getString("currency"));
            }

            return userCoins;

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static int getUserCurrencyId(long chat_id, String coin) {

        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT `currency_id` FROM `users_currencies` LEFT JOIN `currencies` ON `currency_id` WHERE (`user_id` = (SELECT `user_id` FROM `users` WHERE `chat_id` = " + chat_id + ")) AND (`currency` = '" + coin + "')";

            ResultSet res = stat.executeQuery(sql);

            while (res.next()) {
                return res.getInt("currency_id");
            }
            return -1;

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return -1;
        }
    }

    // 100%
    public static int getUserIdByChatId(long chat_id) {
        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT `id` FROM `users` WHERE `chat_id` = " + chat_id;

            ResultSet res = stat.executeQuery(sql);

            while (res.next()) {
                return res.getInt("id");
            }
            return -1;

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return -1;
        }
    }

    public static long getChatIdByUserId(int id) {
        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT `chat_id` FROM `users` WHERE `id` = " + id;

            ResultSet res = stat.executeQuery(sql);

            while (res.next()) {
                return res.getLong("chat_id");
            }
            return -1;

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return -1;
        }
    }

    // 100%
    public static int getCurrencyIdByCoin(String coin) {
        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT `id` FROM `currencies` WHERE `currency` = '" + coin + "'";

            ResultSet res = stat.executeQuery(sql);

            while (res.next()) {
                return res.getInt("id");
            }
            return -1;

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return -1;
        }
    }

    // 100%
    public static String getCoinByCurrencyId(int currency_id) {
        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT `currency` FROM `currencies` WHERE `id` = " + currency_id;

            ResultSet res = stat.executeQuery(sql);

            while (res.next()) {
                return res.getString("currency");
            }
            return null;

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return null;
        }
    }

    // 100%
    public static boolean saveUserCoinsToDB(long chat_id, ArrayList<String> coins) {

        try (Connection con = DriverManager.getConnection(url, userName, password)) {
//            System.out.println("Connection done!");

            Statement stat = con.createStatement();

            int user_id = getUserIdByChatId(chat_id);

            String sql = "INSERT INTO `users_currencies` VALUES ";

            for (String coin : coins) {
                sql += "(null, " + user_id + ", " + getCurrencyIdByCoin(coin) + "),";
            }

            sql = sql.substring(0, sql.length() - 1); // delete last , (comma)

            stat.executeUpdate(sql);
            System.out.println("sql Insert done!");
            return true;
        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return false;
        }
    }


    public static boolean saveAllCoinsToDB(ArrayList<String> coins) {
        try (Connection con = DriverManager.getConnection(url, userName, password)) {
//            System.out.println("Connection done!");

            Statement stat = con.createStatement();

            for (String coin : coins) {
                String sql = "INSERT INTO `currencies` (currency) VALUES ('" + coin + "')";
                stat.executeUpdate(sql);
//                System.out.println("sql Insert done!");
            }

            return true;
        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return false;
        }
    }

    // 100%
    public static boolean saveUserPriceLevel(long chat_id, String coin, Double priceLevel, boolean isHigherLevel) {

        try (Connection con = DriverManager.getConnection(url, userName, password)) {
//            System.out.println("Connection done!");

            Statement stat = con.createStatement();

            int user_id = getUserIdByChatId(chat_id);

            String sql =
                    "INSERT INTO `users_price_levels`(`user_id`, `currency_id`, `price_level`, `is_higher_level`) VALUES ("
                            + user_id + ", " + getCurrencyIdByCoin(coin) + ", " + priceLevel + "," + (isHigherLevel ? 1 : 0) + ")";

            stat.executeUpdate(sql);
            System.out.println("sql Insert done!");
            return true;
        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return false;
        }
    }

    // 100%
    // Actual -> is_notified = 0
    public static ArrayList<UserPriceLevel> getActualUsersPriceLevels() {
        ArrayList<UserPriceLevel> userPriceLevels = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT `id`, `user_id`,`currency_id`,`price_level`,`is_higher_level`,`is_notified` FROM `users_price_levels` WHERE `is_notified` = 0";

            ResultSet res = stat.executeQuery(sql);

            while (res.next()) {
                UserPriceLevel userPriceLevel = new UserPriceLevel();
                userPriceLevel.setId(res.getInt("id"));
                userPriceLevel.setUser_id(res.getInt("user_id"));
                userPriceLevel.setCurrency_id(res.getInt("currency_id"));
                userPriceLevel.setPrice_level(res.getDouble("price_level"));
                userPriceLevel.setIs_higher_level(res.getBoolean("is_higher_level"));
                userPriceLevel.setIs_notified(res.getBoolean("is_notified"));
                userPriceLevels.add(userPriceLevel);
            }

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
        }

        return userPriceLevels;
    }

    public static void setIsNotifiedTrue(int id) {

        try (Connection con = DriverManager.getConnection(url, userName, password)) {
            System.out.println("Connection done!");

            Statement stat = con.createStatement();

            String sql = "UPDATE `users_price_levels` SET `is_notified` = 1 WHERE `id` = " + id;
            stat.executeUpdate(sql);
            System.out.println("sql Update done!");

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
        }
    }

    public static boolean saveUserAddressToDB(long chat_id, String coin, String address, double volume) {

        try (Connection con = DriverManager.getConnection(url, userName, password)) {
//            System.out.println("Connection done!");

            Statement stat = con.createStatement();

            int user_id = getUserIdByChatId(chat_id);

            String sql;

            if (address == null) {
                sql = "INSERT INTO `users_portfolios` (`user_id`, `currency_id`, `volume`) VALUES (null, " + getUserIdByChatId(chat_id) +
                        "," + getCurrencyIdByCoin(coin) + "," + volume + ")";
            } else {
                sql = "INSERT INTO `users_portfolios` (`user_id`, `currency_id`, `address`, `volume`) VALUES (" +
                        getUserIdByChatId(chat_id) + "," +
                        getCurrencyIdByCoin(coin) + ",'" +
                        address + "'," +
                        volume + ")";
            }

            System.out.println(getUserIdByChatId(chat_id));
            System.out.println(getCurrencyIdByCoin(coin));
            System.out.println(address);
            System.out.println(volume);

            stat.executeUpdate(sql);
            System.out.println("sql Insert done!");
            return true;
        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
            return false;
        }
    }

    public static ArrayList<UserPortfolio> getUserPortfoliosByUserId(int user_id) {
        System.out.println(user_id);
        ArrayList<UserPortfolio> userPortfolios = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, userName, password)) {

            Statement stat = con.createStatement();
            String sql = "SELECT `id`, `user_id`,`currency_id`,`address`,`volume` FROM `users_portfolios` WHERE `user_id` = " + user_id;

            ResultSet res = stat.executeQuery(sql);

            while (res.next()) {
                UserPortfolio userPortfolio = new UserPortfolio();
                userPortfolio.setId(res.getInt("id"));
                userPortfolio.setUser_id(res.getInt("user_id"));
                userPortfolio.setCurrency_id(res.getInt("currency_id"));
                userPortfolio.setAddress(res.getString("address"));
                userPortfolio.setVolume(res.getDouble("volume"));
                userPortfolios.add(userPortfolio);
            }

        } catch (SQLException ex) {
            System.out.println("Something wrong with connection to DB...");
            ex.printStackTrace();
        }

        return userPortfolios;
    }

}
