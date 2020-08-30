import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class Binance {

    //    9d44f052272379f446d8da2d428f3312
    public static String getSymbolInfo(String symbol, Model model) throws IOException {
        URL url = new URL("https://api.binance.com/api/v1/ticker/price?symbol=" + symbol);

        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";
        while (in.hasNext()) {
            result += in.nextLine();
        }

        JSONObject object = new JSONObject(result);
        model.setSymbol(object.getString("symbol"));
        model.setPrice(object.getDouble("price"));

        return "Symbol: " + model.getSymbol() + "\n" +
                "Price: " + model.getPrice();
    }

}