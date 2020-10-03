import com.google.common.base.Strings;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.io.IOException;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private final int ADDRESS_LENGTH = 11;

    private static ArrayList<Symbol> symbols = new ArrayList<>();
    private static ArrayList<String> allCoins = new ArrayList<>();
    private static ArrayList<String> userCoins = new ArrayList<>(); // current user coins
    private static TreeSet<String> сoinsForDelete = new TreeSet<>(); // current user coins
    private static Integer add_watch_list_MessageId = -1;
    private static Integer del_watch_list_MessageId = -1;
    private static Integer show_watch_list_MessageId = -1;
    private static Integer add_price_level_MessageId = -1;
    private static Integer add_addresses_to_watch_MessageId = -1;
    private static Integer user_entered_wallet_balance = -1;
    private static String BOT_USERNAME;// = "mycryptoinfomvc777bot";
    private static String TOKEN;// = "1340695166:AAGMWgwQFoKYSCaK8DObbkHdPx0KGJWynJk";

    private static String lang = "ru"; // "en"

    public static void main(String[] args) {

//        String ETH_address = "0x6431103b981fbf6e0d6215a9e885e68942672ceb";
//        String BTC_address = "bc1qy8wyq6wt7mlu22haa83f4647g863aknquhpcqq";

        FileInputStream fis;
        Properties property = new Properties();

        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            property.load(fis);

            BOT_USERNAME = property.getProperty("BOT_USERNAME");
            TOKEN = property.getProperty("TOKEN");

            System.out.println("BOT_USERNAME: " + BOT_USERNAME
                    + ", TOKEN: " + TOKEN);

        } catch (IOException e) {
            System.err.println("ОШИБКА: Файл свойств отсуствует!");
        }

        return;

/*        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            Bot.startThreadForCheckPriceLevels();
            Bot.startThreadForCheckWalletBalanceChange();
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Java realization in PHP http_build_query () effect
     *
     * @param array Key = value form of two-digit group
     * @return
     */
    public static String http_build_query(Map<String, String> array) {
        String reString = "";
        // iterate string formed akey = avalue & bkey = bvalue & ckey = cvalue form of
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry) it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            reString += key + "=" + value + "&";
        }
        reString = reString.substring(0, reString.length() - 1);
        // string obtained by processing the obtained target format string
        try {
            reString = java.net.URLEncoder.encode(reString, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        reString = reString.replace("%3D", "=").replace("%26", "&");
        return reString;
    }

    private static void startThreadForCheckWalletBalanceChange() {

        Thread checkIsAnyWalletBalanceChange_Thread = new Thread(() -> {

            while (true) {
                ArrayList<UserPortfolio> userPortfolios = DBManager.getAllUserPortfolios();
                long old_chat_id = 0;

                for (UserPortfolio userPortfolio : userPortfolios) {
                    Long id = userPortfolio.getId();
                    String coin = DBManager.getCoinByCurrencyId(userPortfolio.getCurrency_id());
                    coin = coin.substring(0, 3);
                    String address = userPortfolio.getAddress();
                    Double oldBalance = userPortfolio.getBalance();
                    Double newBalance = Blockcypher.getBalanceByAnyWalletAddress(address);

                    Double diff = newBalance - oldBalance;

                    if (Math.abs(diff) >= 0.00000001) {

                        System.out.println(address + ": " + oldBalance + " -> " + newBalance + (diff >= 0 ? "+" + diff : diff));

                        // notify user

                        Long chat_id = DBManager.getChatIdByUserId(userPortfolio.getUser_id());

                        if (old_chat_id == chat_id) {
                            try {
                                Thread.sleep(1000 / 20); // Telegram API ограничение 30 сообщений в секунду
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        sendMessage(chat_id, "!!! Изменился баланс " + coin + "-кошелька " + address + ":\n" +
                                String.format("%.8f", oldBalance) + " -> " + String.format("%.8f", newBalance) +
                                " (" + (diff >= 0 ? "+" + String.format("%.8f", diff) : String.format("%.8f", diff)) + ")");

                        userPortfolio.setBalance(newBalance);

                        old_chat_id = chat_id;

                        // save new balance to DB
                        DBManager.updateWalletBalanceInUsersPortfolios(id, newBalance);
                    }
                }

                try {
                    Thread.sleep(61000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        checkIsAnyWalletBalanceChange_Thread.start();
    }

    private static void startThreadForCheckPriceLevels() {

        Thread checkIsUserPriceLevelReached_Thread = new Thread(() -> {

            while (true) {
                ArrayList<UserPriceLevel> usersPriceLevels = DBManager.getActualUsersPriceLevels();
                long old_chat_id = 0;

                try {
                    getAllCoins();
                    for (UserPriceLevel userPriceLevel : usersPriceLevels) {
                        String coin = DBManager.getCoinByCurrencyId(userPriceLevel.getCurrency_id());
                        Long id = userPriceLevel.getId();
                        double price_level = userPriceLevel.getPrice_level();
                        boolean is_higher_level = userPriceLevel.isIs_higher_level();
                        Symbol symbol = symbols.get((int) (userPriceLevel.getCurrency_id() - 1));
                        Double cur_price = symbol.getPrice();

                        if (((cur_price <= price_level) && !is_higher_level) ||
                                ((cur_price >= price_level) && is_higher_level)) {

                            // notify user

                            long chat_id = DBManager.getChatIdByUserId(userPriceLevel.getUser_id());

                            if (old_chat_id == chat_id) {
                                Thread.sleep(1000 / 20); // Telegram API ограничение 30 сообщений в секунду
                            }

                            sendMessage(chat_id, "!!! Достигнут ценовой уровень: " + price_level);
                            try {
                                sendMessage(chat_id, "!!! Текущий курс " + coin + ":\n" + Binance.getSymbolInfo(coin, new Symbol()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            DBManager.setIsNotifiedTrue(id);

                            old_chat_id = chat_id;
                        }
                    }
                } catch (InterruptedException e) {//| IOException
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(60000); // 1 time per 1 minute
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        checkIsUserPriceLevelReached_Thread.start();
    }

    public static void sendMessage(long chat_id, String message) {
        try {
            Map<String, String> http_query = new HashMap<>();
            http_query.put("text", message);
            String text = http_build_query(http_query);

            URL url = new URL("https://api.telegram.org/bot" + TOKEN + "/sendMessage?chat_id=" + chat_id + "&" + text);
            System.out.println(url);
            url.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getAllCoins() {
        // maybe it should be in parallel threat
        try {
            symbols = Binance.getAllSymbols();
            allCoins = new ArrayList<>(symbols.size());

            for (Symbol symbol : symbols) {
                String coin = symbol.getSymbol();
                allCoins.add(coin);
            }
/* !!!!!!!!
            // update `currencies` table 1 time per every day
            DBManager.truncateTable("currencies");
            DBManager.saveAllCoinsToDB(allCoins);
            */
//            printModels(models);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printSymbols(ArrayList<Symbol> symbols) {
        for (Symbol symbol : symbols) {
            System.out.println(symbol);
        }
    }

    public void sendMsgToChat(long chat_id, String text) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);

        try {
            setButtons(sendMessage);
//            execute(sendMessage);
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            // do nothing... for not showing messages
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String text, boolean isReplyToMessage) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());

        if (isReplyToMessage) {
            sendMessage.setReplyToMessageId(message.getMessageId());
        }

        sendMessage.setText(text);

        try {
            setButtons(sendMessage);
//            execute(sendMessage);
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            // do nothing... for not showing messages
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();
        Symbol symbol = new Symbol();
        long chat_id = message.getChatId();
        DBManager.insertNewUser(chat_id);
        userCoins = DBManager.getUserCoins(chat_id);
//        System.out.println(update.getMessage().getText()); // delete in the end of developing

        if (message != null && message.hasText()) {

            String msgText = message.getText();

            switch (msgText) {
                case "ℹ️ Справка":
                case "/help":
                    sendMsg(message, "Чем могу помочь?", true);
                    break;
                case "⚙ Настройки":
                case "/settings":
                    sendMsg(message, "Что будем настраивать?", true);
                    break;
                case "\uD83D\uDCB0 Добавить курсы":
                case "/add_watch_list":
                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                    add_watch_list_MessageId = message.getMessageId();
                    sendMsg(message, "Введите список существующих криптовалют через пробел(ы) или с новых строк:", true);
                    break;
                case "⌫ Удалить курсы":
                case "/del_watch_list":
                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                    del_watch_list_MessageId = message.getMessageId();
                    sendMsg(message, "Введите список криптовалют через пробел(ы) или с новых строк:", true);
                    break;
                case "\uD83D\uDCC8 Показать курсы":
                case "/show_watch_list":
                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                    show_watch_list_MessageId = message.getMessageId();
                    DBManager.getUserCoins(chat_id);

                    sendMsg(message, "Ваши отслеживаемые курсы:\n", true);
                    for (String userCoin : userCoins) {
                        sendCoinInfo(userCoin, symbol, message, false);
                    }
                    break;
                case "\uD83D\uDCC9 Добавить уровень цены для уведомления":
                case "/add_price_level":
                    add_price_level_MessageId = message.getMessageId();
                    sendMsg(message, "При достижении цены до заданного уровня Вам придет уведомление.\nВведите курс и цену через пробел:", false);
                    break;

                case "\uD83D\uDCBC Показать кошельки":
                    ArrayList<UserPortfolio> userPortfolios = DBManager.getUserPortfoliosByUserId(DBManager.getUserIdByChatId(message.getChatId()));
                    int[] maxLengths = {0, 0, 0};

                    Double finalBalanceInDollars = 0.0;
                    ArrayList<Double> volumes = new ArrayList<>(userPortfolios.size());

                    for (UserPortfolio userPortfolio : userPortfolios) {

                        String coin = DBManager.getCoinByCurrencyId(userPortfolio.getCurrency_id());
                        String address = userPortfolio.getAddress();
                        Double volume = Blockcypher.getBalanceByAnyWalletAddress(address);   //String.format("%.8f", userPortfolio.getVolume());
                        volumes.add(volume);

                        Symbol m = new Symbol();
                        try {
                            Binance.getSymbolInfo(coin, m);
                            finalBalanceInDollars += volume * m.getPrice();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (coin != null && maxLengths[0] < coin.length()) {
                            maxLengths[0] = coin.length();
                        }
                        if (maxLengths[1] < address.length()) {
                            maxLengths[1] = address.length();
                        }
                        if (maxLengths[2] < volume.toString().length()) {
                            maxLengths[2] = volume.toString().length();
                        }
                    }

                    String values = "";

                    int j = 0;
                    for (UserPortfolio userPortfolio : userPortfolios) {
                        String coin = DBManager.getCoinByCurrencyId(userPortfolio.getCurrency_id());
                        String volume = String.format("%.8f", volumes.get(j++));
                        coin = coin.substring(0, 3) + " ";
                        values += "|" +
                                getPaddingString(coin, coin.length() - 1, PaddingType.CENTER) + "|" +
                                getPaddingString(cutString(userPortfolio.getAddress(), ADDRESS_LENGTH), ADDRESS_LENGTH, PaddingType.CENTER) + "|" +
                                getPaddingString(volume, volume.length(), PaddingType.CENTER) + "|\n";
                    }

                    String msg = "<code>\n" +
                            "|" +
                            getPaddingString("$$$", maxLengths[0] - 3, PaddingType.CENTER) + " |" +
                            getPaddingString("Address", ADDRESS_LENGTH + 5, PaddingType.CENTER) + "|" +
                            getPaddingString("Balance", maxLengths[2] + 4, PaddingType.CENTER) + " |\n" +

                            "|" + Strings.repeat("-", maxLengths[0] - 3) +
                            "|" + Strings.repeat("-", ADDRESS_LENGTH) +
                            "|" + Strings.repeat("-", maxLengths[2]) + "|\n" +

                            values +

                            "</code>&parse_mode=HTML";
                    sendMessage(message.getChatId(), msg);

                    msg = "Total balance, $: " + String.format("%.2f", finalBalanceInDollars);
                    sendMessage(message.getChatId(), msg);

                    break;

                case "\uD83D\uDCBC Добавить кошельки":
                case "/add_addresses_to_watch":
                    add_addresses_to_watch_MessageId = message.getMessageId();
                    sendMsg(message, "Введите адрес кошелька:", false);
                    break;
                default:
                    if (del_watch_list_MessageId.equals(message.getMessageId() - 2)) {

                        String[] coins = msgText.split("\\s+"); // \\s+ -- любое сочетание пробельных символов
/*
                            switch (lang) {
                                case "ru":
                                    System.out.println("Вы ввели такие монеты: "); // + msgText);
                                    break;
                                case "en":
                                    System.out.println("You entered this coins: "); // + msgText);
                                    break;
                            }*/

                        for (int i = 0; i < coins.length; i++) {
                            coins[i] = convertSymbolName(coins[i]);
                        }

                        deleteUserCoins(coins);

                        // удалять только монеты, которые есть в списке
                        DBManager.deleteUserCoinsFromDB(chat_id, сoinsForDelete);

                        switch (lang) {
                            case "ru":
                                System.out.println("\nУдалённые монеты:\n\n");
                                break;
                            case "en":
                                System.out.println("\nDeleted coins:\n\n");
                                break;
                        }

                        String coinsNames = "";

                        for (String сoinForDelete : сoinsForDelete) {
                            System.out.println(сoinForDelete);
                            coinsNames += сoinForDelete + "\n";
                        }

                        сoinsForDelete.clear();

                        switch (lang) {
                            case "ru":
                                sendMsg(message, "Удаленные монеты:\n" + coinsNames, true);
                                break;
                            case "en":
                                sendMsg(message, "Deleted coins:\n" + coinsNames, true);
                                break;
                        }

                    } else
                        // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                        if (add_price_level_MessageId.equals(message.getMessageId() - 2)) {

                            String[] coin_price = msgText.split("\\s+"); // \\s+ -- любое сочетание пробельных символов
                            String coin = coin_price[0];
                            String priceLevelStr = coin_price[1].replace(',', '.');
                            coin = convertSymbolName(coin);

                            boolean isGoodCoin = false;
                            boolean isGoodPrice = false;

                            try {
                                if (allCoins.contains(coin)) {
                                    isGoodCoin = true;
                                    // берем текущую цену курса
                                    Binance.getSymbolInfo(coin, symbol);
                                } else {
                                    throw new IOException();
                                }
                            } catch (IOException e) {

                                switch (lang) {
                                    case "ru":
                                        sendMsg(message, "Неизвестный символ.", true);
                                        break;
                                    case "en":
                                        sendMsg(message, "Unknown symbol.", true);
                                        break;
                                }
//                            e.printStackTrace();
                            }

                            double priceLevel = 0.0;

                            try {
                                priceLevel = Double.parseDouble(priceLevelStr);
                                isGoodPrice = true;
                            } catch (NumberFormatException e) {
                                sendMsg(message, "Ошибка в цене:" + priceLevelStr, true);
                                e.printStackTrace();
                            }

                            if (isGoodCoin && isGoodPrice) {
                                sendMsg(message, "Вы ввели курс: " + coin + "\nПо цене: " + priceLevel + "\nПри достижении цены Вы получите уведомление.\nЖирного Вам профита!", false);
                                boolean isHigherLevel = priceLevel > symbol.getPrice(); // введенная цена выше (1) или ниже/равна (0) текущей цены
                                DBManager.saveUserPriceLevel(chat_id, coin, priceLevel, isHigherLevel);
                            }

                        } else
                            // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                            if (add_watch_list_MessageId.equals(message.getMessageId() - 2)) {

                                String[] coins = msgText.split("\\s+"); // \\s+ -- любое сочетание пробельных символов
/*
                            switch (lang) {
                                case "ru":
                                    System.out.println("Вы ввели такие монеты: "); // + msgText);
                                    break;
                                case "en":
                                    System.out.println("You entered this coins: "); // + msgText);
                                    break;
                            }*/

                                for (int i = 0; i < coins.length; i++) {
                                    coins[i] = convertSymbolName(coins[i]);
                                }

                                filterUserCoins(coins);

                                // добавлять только монеты, которых ещё нет в списке
                                DBManager.saveUserCoinsToDB(chat_id, userCoins);

                                switch (lang) {
                                    case "ru":
                                        System.out.println("\nДобавленные монеты:\n\n");
                                        break;
                                    case "en":
                                        System.out.println("\nAdded coins:\n\n");
                                        break;
                                }

                                String coinsNames = "";

                                for (String userCoin : userCoins) {
                                    System.out.println(userCoin);
                                    coinsNames += userCoin + "\n";
                                }

                                switch (lang) {
                                    case "ru":
                                        sendMsg(message, "Добавленные монеты:\n" + coinsNames, true);
                                        break;
                                    case "en":
                                        sendMsg(message, "Added coins:\n" + coinsNames, true);
                                        break;
                                }

                            } else
                                // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                                if (user_entered_wallet_balance.equals(message.getMessageId() - 2)) {
                                    if (Double.parseDouble(msgText) < 0) {
                                        sendMsg(message, "Баланс кошелька не может быть отрицательным", true);
                                        user_entered_wallet_balance = message.getMessageId();
                                    }
                                } else
                                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                                    if (add_addresses_to_watch_MessageId.equals(message.getMessageId() - 2)) {
                                        String address = msgText;
                                        System.out.println("address " + address);

                                        // 2 проверки, т.к. на 1й сайт может забанить запрос
                                        if (Blockcypher.isValidAddress1(address) == null && Blockcypher.isValidAddress2(address) == null) {
                                            sendMsg(message, "Неверный адрес кошелька!", true);
                                        } else {
                                            try {
                                                Thread.sleep(500);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            // Валидный адрес кошелька => просим ввести объём если не получается спарсить
                                            String coin = Blockcypher.getCoinByWalletAddress(address);
                                            Double finalBalance = Blockcypher.getBalanceBy_BTC_ETH_WalletAddress(address, coin);
                                            if (finalBalance == null) {
                                                finalBalance = Blockcypher.getBalanceByAnyWalletAddress(address);
                                            }
                                            if (finalBalance == null) {
                                                sendMsg(message, "Введите баланс кошелька (не удалось получить):", false);
                                                user_entered_wallet_balance = message.getMessageId();
                                            } else {
                                                // добавляем монету, адрес, баланс в БД
                                                coin = convertSymbolName(coin);
                                                DBManager.saveUserAddressToDB(message.getChatId(), coin, address, finalBalance);

                                                // Create QR-code from wallet address
                                                //sendMsg(message, "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + address, false);
                                            }
                                        }
                                    } else {
                                        if (msgText.length() < 10) {
                                            sendCoinInfo(msgText, symbol, message, true);
                                        }
                                    }
                    break;
            }
        }
    }

    public static String cutString(String s, int finalLength) {
        String res = s.substring(0, finalLength / 2);
        int rest = s.length() - (finalLength / 2);
        res += "…" + s.substring(rest);
        return res;
    }

    private void sendCoinInfo(String coinName, Symbol symbol, Message message, boolean isReplyToMessage) {
        coinName = convertSymbolName(coinName);

        try {
            sendMsg(message, Binance.getSymbolInfo(coinName, symbol), isReplyToMessage);
        } catch (IOException e) {
            switch (lang) {
                case "ru":
                    sendMsg(message, "Неизвестный символ.", true);
                    break;
                case "en":
                    sendMsg(message, "Unknown symbol.", true);
                    break;
            }
        }
    }

    private String convertSymbolName(String msgText) {
        msgText = msgText.toUpperCase();
        msgText += msgText.length() == 3 ? "USDT" : "";
        msgText += msgText.endsWith("USD") ? "T" : "";
        return msgText;
    }

    private static void deleteUserCoins(String[] coins) {
        for (String coin : coins) {
            if (allCoins.contains(coin)) {
                if (userCoins.contains(coin)) {
                    userCoins.remove(coin);
                    сoinsForDelete.add(coin);
                } else {
                    switch (lang) {
                        case "ru":
                            System.out.println("Курс \"" + coin + "\" уже отсутствует в Вашем списке курсов.");
                            break;
                        case "en":
                            System.out.println("Coin \"" + coin + "\" is already not exists in your coins list.");
                            break;
                    }

                }
            } else {
                switch (lang) {
                    case "ru":
                        System.out.println("Курс \"" + coin + "\" отсутствует на бирже BINANCE.");
                        break;
                    case "en":
                        System.out.println("Coin \"" + coin + "\" doesn't exist on BINANCE.");
                        break;
                }
            }
        }
    }

    private static void filterUserCoins(String[] coins) {
        userCoins.clear();
        for (String coin : coins) {
            if (allCoins.contains(coin)) {
                if (!userCoins.contains(coin)) {
                    userCoins.add(coin);
                } else {
                    switch (lang) {
                        case "ru":
                            System.out.println("Курс \"" + coin + "\" уже присутствует в Вашем списке курсов.");
                            break;
                        case "en":
                            System.out.println("Coin \"" + coin + "\" is already exists in user's coins list.");
                            break;
                    }
                }
            } else {
                switch (lang) {
                    case "ru":
                        System.out.println("Курс \"" + coin + "\" отсутствует на бирже BINANCE.");
                        break;
                    case "en":
                        System.out.println("Coin \"" + coin + "\" doesn't exist on BINANCE.");
                        break;
                }
            }
        }
    }

    public void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardRow keyboardRow4 = new KeyboardRow();

        keyboardRow1.add(new KeyboardButton("\uD83D\uDCB0 Добавить курсы"));
        keyboardRow1.add(new KeyboardButton("⌫ Удалить курсы"));
        keyboardRow1.add(new KeyboardButton("\uD83D\uDCC8 Показать курсы"));
        keyboardRow2.add(new KeyboardButton("\uD83D\uDCC9 Добавить уровень цены для уведомления"));
        keyboardRow3.add(new KeyboardButton("\uD83D\uDCBC Добавить кошельки"));
        keyboardRow3.add(new KeyboardButton("\uD83D\uDCBC Показать кошельки"));
        keyboardRow4.add(new KeyboardButton("⚙ Настройки"));
        keyboardRow4.add(new KeyboardButton("ℹ️ Справка"));

        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        keyboardRowList.add(keyboardRow3);
        keyboardRowList.add(keyboardRow4);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    enum PaddingType {
        LEFT,
        RIGHT,
        CENTER
    }

    public static String getPaddingString(String s, int finalLength, PaddingType paddingType) {
        int sLength = s.length();
        finalLength -= 2;
        if (finalLength < s.length()) {
            return s;
//            return " " + s + " ";
        }
        int diff = finalLength - sLength - 1;
        diff = diff <= 0 ? 1 : diff;
        diff -= 2;
        switch (paddingType) {
            case LEFT:
                return String.format("%" + diff + "s", " ") + s;
//                return String.format("%" + diff + "s", " ") + s + " ";
            case RIGHT:
                return s + String.format("%" + diff + "s", " ");
//            return " " + s + String.format("%" + diff + "s", " ");
            case CENTER:
                int half = diff / 2;
                half = Math.max(half, 1);
                int rest = diff - half;
                rest = Math.max(rest, 1);
                return String.format("%" + (half) + "s", " ") + s + String.format("%" + rest + "s", " ");
//            return String.format("%" + (half + 1) + "s", " ") + s + String.format("%" + rest + "s", " ");
        }
        return null;
    }

    public String getBotUsername() {
        return BOT_USERNAME;
    }

    public String getBotToken() {
        return TOKEN;
    }
}

