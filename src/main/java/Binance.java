import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Binance {

    //    9d44f052272379f446d8da2d428f3312
    public static String getSymbolInfo(String symbol, Model model) throws IOException {
        URL url = new URL("https://api.binance.com/api/v1/ticker/price?symbol=" + symbol);

        JSONObject jsonObject = Binance.getJSONFromURL(url);

        model.setSymbol(jsonObject.getString("symbol"));
        model.setPrice(jsonObject.getDouble("price"));

        return "Symbol: " + model.getSymbol() + "\n" +
                "Price: " + model.getPrice();
    }

    private static JSONObject getJSONFromURL(URL url) throws IOException {
        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";

        while (in.hasNext()) {
            result += in.nextLine();
        }

        return new JSONObject(result);
    }

    public static ArrayList<Model> getAllSymbols() throws IOException {
        URL url = new URL("https://api.binance.com/api/v1/ticker/price");

        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";
        while (in.hasNext()) {
            result += in.nextLine();
        }

        JSONArray jsonArray = new JSONArray(result);

        int jsonArrLength = jsonArray.length();
        ArrayList<Model> models = new ArrayList<>(jsonArrLength);

        for (int i = 0; i < jsonArrLength; i++) {
            Model model = new Model();
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            model.setSymbol(jsonObj.getString("symbol"));
            model.setPrice(jsonObj.getDouble("price"));
            models.add(model);
        }

        return models;
    }
}