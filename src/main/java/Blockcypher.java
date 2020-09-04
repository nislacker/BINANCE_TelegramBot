import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


public class Blockcypher {
//    https://api.blockcypher.com/v1/btc/main/addrs/bc1qy8wyq6wt7mlu22haa83f4647g863aknquhpcqq/balance

    public static Double getBalanceByAnyWalletAddress(String address) {
        try {
            URLConnection con = new URL("https://www.blockchain.com/en/search?search=" + address).openConnection();
            con.connect();
            InputStream is = con.getInputStream();
            URL redirectedURL = con.getURL();

            System.out.println(redirectedURL);
            Document doc = Jsoup.connect(redirectedURL.toString()).get();
            String classes = "div > span";
            Elements spans = doc.select(classes);
            Double finalBalance;
            boolean isNextFinalBalance = false;
            for (Element span : spans) {
                if (isNextFinalBalance) {
                    finalBalance = Double.valueOf(span.text().trim().split(" ")[0]);
                    return finalBalance;
                }
                if (span.text().trim().equals("Final Balance")) {
                    isNextFinalBalance = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // для BTC и ETH в 2 раза быстрее чем метод getBalanceByAnyWalletAddress, но другие монеты не знает
    public static double getBalanceBy_BTC_ETH_WalletAddress(String address, String coin) {
        Double balance = null;

        try {
            URL url = new URL("https://api.blockcypher.com/v1/" + coin + "/main/addrs/" + address + "/balance");

            JSONObject jsonObject = null;

            jsonObject = Binance.getJSONFromURL(url);
            balance = jsonObject.getDouble("final_balance");

            balance /= 100_000_000; // satoshi -> normal balance

            if (coin.equals("eth")) {
                balance /= 10_000_000_000.0;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return balance;
    }

    public static String getCoinByWalletAddress(String address) {
        try {
            URLConnection con = new URL("https://www.blockchain.com/ru/search?search=" + address).openConnection();
            con.connect();
            InputStream is = con.getInputStream();
            URL redirectedURL = con.getURL();

            String[] pathElements = redirectedURL.getPath().split("/");
            String coin = pathElements[pathElements.length - 3];
            is.close();
            return coin;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Boolean isValidAddress1(String address) {
        try {
            URLConnection con = new URL("https://www.blockchain.com/en/search?search=" + address).openConnection();
//            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            con.setConnectTimeout(10000);
//            con.setRequestProperty("method", "POST");
            con.connect();
            InputStream is = con.getInputStream();
            URL redirectedURL = con.getURL();
            boolean res = redirectedURL.getPath().contains("error");
            is.close();

            return res;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Boolean isValidAddress2(String address) {
        try {
            URL url = new URL("http://addressvalidator.evzpav.com/validate/" +
                    Blockcypher.getCoinByWalletAddress(address) +
                    "/" + address);
            JSONObject jsonObject = null;
            jsonObject = Binance.getJSONFromURL(url);
            return jsonObject.getBoolean("valid");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Boolean isValidAddress2(String address, String coin) {
        try {
            URL url = new URL("http://addressvalidator.evzpav.com/validate/" + coin + "/" + address);
            JSONObject jsonObject = null;
            jsonObject = Binance.getJSONFromURL(url);
            return jsonObject.getBoolean("valid");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
