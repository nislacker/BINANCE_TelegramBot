import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Binance {

    public static String getSymbolInfo(String symbol, Symbol symbolModel) throws IOException {
        URL url = new URL("https://api.binance.com/api/v1/ticker/price?symbol=" + symbol);

        JSONObject jsonObject = Binance.getJSONFromURL(url);

        symbolModel.setSymbol(jsonObject.getString("symbol"));
        symbolModel.setPrice(jsonObject.getDouble("price"));

        return "Symbol: " + symbolModel.getSymbol() + "\n" +
                "Price: " + symbolModel.getPrice();
    }

    public static JSONObject getJSONFromURL(URL url) throws IOException {
        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";

        while (in.hasNext()) {
            result += in.nextLine();
        }

        return new JSONObject(result);
    }

    public static ArrayList<Symbol> getAllSymbols() throws IOException {
        URL url = new URL("https://api.binance.com/api/v1/ticker/price");

        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";
        while (in.hasNext()) {
            result += in.nextLine();
        }

        JSONArray jsonArray = new JSONArray(result);

        int jsonArrLength = jsonArray.length();
        ArrayList<Symbol> symbols = new ArrayList<>(jsonArrLength);

        for (int i = 0; i < jsonArrLength; i++) {
            Symbol symbolModel = new Symbol();
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            symbolModel.setSymbol(jsonObj.getString("symbol"));
            symbolModel.setPrice(jsonObj.getDouble("price"));
            symbols.add(symbolModel);
        }

        return symbols;
    }
}