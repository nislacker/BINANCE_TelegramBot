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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private static ArrayList<Model> models = new ArrayList<>();
    private static ArrayList<String> allCoins = new ArrayList<>();
    private static ArrayList<String> userCoins = new ArrayList<>(); // current user coins
    private static Integer add_watch_list_MessageId = -1;
    private static Integer show_watch_list_MessageId = -1;
    private static Integer add_price_level_MessageId = -1;
    private static final String BOT_USERNAME = "mycryptoinfomvc777bot";
    private static final String TOKEN = "1340695166:AAGMWgwQFoKYSCaK8DObbkHdPx0KGJWynJk";

    private static String lang = "ru"; // "en"

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            Bot.startThread();
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Java realization in PHP http_build_query () effect
     * @param array
     * Key = value form of two-digit group
     * @return
     */
    public static String http_build_query(Map<String ,String> array){
        String reString = "";
        // iterate string formed akey = avalue & bkey = bvalue & ckey = cvalue form of
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> entry =(Map.Entry) it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            reString += key+"="+value+"&";
        }
        reString = reString.substring(0, reString.length()-1);
        // string obtained by processing the obtained target format string
        try {
            reString = java.net.URLEncoder.encode(reString,"utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        reString = reString.replace("%3D", "=").replace("%26", "&");
        return reString;
    }

    private static void startThread() {

        Thread checkIsUserPriceLevelReached_Thread = new Thread(() -> {

            while (true) {
                ArrayList<UserPriceLevel> usersPriceLevels = DBManager.getActualUsersPriceLevels();
                long old_chat_id = 0;

                try {
                    getAllCoins();
                    for (UserPriceLevel userPriceLevel : usersPriceLevels) {
                        String coin = DBManager.getCoinByCurrencyId(userPriceLevel.getCurrency_id());
                        int id = userPriceLevel.getId();
                        double price_level = userPriceLevel.getPrice_level();
                        boolean is_higher_level = userPriceLevel.isIs_higher_level();
                        Model model = models.get(userPriceLevel.getCurrency_id() - 1);
                        Double cur_price = model.getPrice();

                        if (((cur_price <= price_level) && !is_higher_level) ||
                                ((cur_price >= price_level) && is_higher_level)) {
                            // notify user

                            long chat_id = DBManager.getChatIdByUserId(userPriceLevel.getUser_id());

                            if (old_chat_id == chat_id) {
                                Thread.sleep(1000/20); // Telegram API ограничение 30 сообщений в секунду
                            }

                            sendMessage(chat_id, "!!! Достигнут ценовой уровень: " + price_level);
                            try {
                                sendMessage(chat_id, "!!! Текущий курс " + coin + ":\n" + Binance.getSymbolInfo(coin, new Model()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            DBManager.setIsNotifiedTrue(id);

                            old_chat_id = chat_id;
                        }
                    }
                } catch (InterruptedException  e) {//| IOException
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
            Map<String, String> http_query = new HashMap<String, String>();
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
            models = Binance.getAllSymbols();
            allCoins = new ArrayList<>(models.size());

            for (Model model : models) {
                String coin = model.getSymbol();
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

    private static void printModels(ArrayList<Model> models) {
        for (Model model : models) {
            System.out.println(model);
        }
    }

    public void sendMsgToChat(long chat_id, String text) {

//        System.out.println(text);

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

//        System.out.println(text);

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



        Model model = new Model(); // ???

        long chat_id = message.getChatId();

        DBManager.insertNewUser(chat_id);

        userCoins = DBManager.getUserCoins(chat_id);

        System.out.println(update.getMessage().getText()); // delete in the end of developing

        if (message != null & message.hasText()) {

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
                case "\uD83D\uDCB0 Добавить новые курсы":
                case "/add_watch_list":
                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                    add_watch_list_MessageId = message.getMessageId();
                    sendMsg(message, "Введите список существующих криптовалют через пробел(ы) или с новых строк:", true);
                    break;
                case "\uD83D\uDCC8 Показать мои курсы":
                case "/show_watch_list":
                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                    show_watch_list_MessageId = message.getMessageId();
                    //DBManager.getUserCoins(chat_id)

                    sendMsg(message, "Ваши отслеживаемые курсы:\n", true);
                    for (String userCoin : userCoins) {
                        sendCoinInfo(userCoin, model, message, false);
                    }
                    break;
                case "\uD83D\uDCC9 Добавить уровень цены для уведомления":
                case "/add_price_level":
                    add_price_level_MessageId = message.getMessageId();
                    sendMsg(message, "При достижении цены до заданного уровня Вам придет уведомление.\nВведите курс и цену через пробел:", false);
                    break;
                default:
//                    System.out.println("add_price_level_MessageId: " + add_price_level_MessageId);
//                    System.out.println("message.getMessageId() - 1: " + (message.getMessageId() - 2));
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
                                Binance.getSymbolInfo(coin, model);
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
                            e.printStackTrace();
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
                            boolean isHigherLevel = priceLevel > model.getPrice(); // введенная цена выше (1) или ниже/равна (0) текущей цены
                            DBManager.saveUserPriceLevel(chat_id, coin, priceLevel, isHigherLevel);
                        }

                    } else
                        // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                        if (add_watch_list_MessageId.equals(message.getMessageId() - 2)) {

                            String[] coins = msgText.split("\\s+"); // \\s+ -- любое сочетание пробельных символов

                            switch (lang) {
                                case "ru":
                                    System.out.println("Вы ввели такие монеты: "); // + msgText);
                                    break;
                                case "en":
                                    System.out.println("You entered this coins: "); // + msgText);
                                    break;
                            }

                            for (int i = 0; i < coins.length; i++) {
                                coins[i] = convertSymbolName(coins[i]);
                                System.out.println(coins[i]);
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

                        } else {
                            sendCoinInfo(msgText, model, message, true);
                        }
                    break;
            }
        }
    }

    private void sendCoinInfo(String coinName, Model model, Message message, boolean isReplyToMessage) {
        coinName = convertSymbolName(coinName);

        try {
            sendMsg(message, Binance.getSymbolInfo(coinName, model), isReplyToMessage);
        } catch (IOException e) {
            switch (lang) {
                case "ru":
                    sendMsg(message, "Неизвестный символ.", true);
                    break;
                case "en":
                    sendMsg(message, "Unknown symbol.", true);
                    break;
            }

            e.printStackTrace();
        }
    }

    private String convertSymbolName(String msgText) {
        msgText = msgText.toUpperCase();
        msgText += msgText.length() == 3 ? "USDT" : "";
        msgText += msgText.endsWith("USD") ? "T" : "";
        return msgText;
    }

    private static void filterUserCoins(String[] coins) {
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
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();

        keyboardFirstRow.add(new KeyboardButton("\uD83D\uDCB0 Добавить новые курсы"));
        keyboardFirstRow.add(new KeyboardButton("\uD83D\uDCC8 Показать мои курсы"));
        keyboardSecondRow.add(new KeyboardButton("\uD83D\uDCC9 Добавить уровень цены для уведомления"));
        keyboardThirdRow.add(new KeyboardButton("⚙ Настройки"));
        keyboardThirdRow.add(new KeyboardButton("ℹ️ Справка"));

        keyboardRowList.add(keyboardFirstRow);
        keyboardRowList.add(keyboardSecondRow);
        keyboardRowList.add(keyboardThirdRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public String getBotUsername() {
        return BOT_USERNAME;
    }

    public String getBotToken() {
        return TOKEN;
    }
}
