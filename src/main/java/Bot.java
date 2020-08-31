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
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static ArrayList<Model> models;
    private static ArrayList<String> allCoins;
    private static ArrayList<String> userCoins = new ArrayList<>(); // current user coins
    private Integer currentMessageId = -1;

    public static void main(String[] args) {

        // maybe it should be in parallel threat
        try {
            models = Binance.getAllSymbols();
            allCoins = new ArrayList<>(models.size());

            for (Model model : models) {
                allCoins.add(model.getSymbol());
            }
//            printModels(models);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        //

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void printModels(ArrayList<Model> models) {
        for (Model model : models) {
            System.out.println(model);
        }
    }

    public void sendMsg(Message message, String text) {

//        System.out.println(text);

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
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

        Model model = new Model();
        Message message = update.getMessage();
//        System.out.println(message.getMessageId());

        System.out.println(update.getMessage().getText()); // delete in the end of developing

        if (message != null & message.hasText()) {

            String msgText = message.getText();

            switch (msgText) {
                case "/help":
                    sendMsg(message, "Чем могу помочь?");
                    break;
                case "/settings":
                    sendMsg(message, "Что будем настраивать?");
                    break;
                case "/addWatchList":
                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                    currentMessageId = message.getMessageId();
//                    System.out.println(currentMessageId);
                    sendMsg(message, "Введите список существующих криптовалют через пробел(ы) или с новых строк:");
                    break;
                default:

                    // !!! КОСТЫЛЬ !!! -- для отслеживания ответа юзера именно на это сообщение бота
                    if (currentMessageId.equals(message.getMessageId() - 2)) {
                        System.out.println("You entered this coins: "); // + msgText);
                        String[] coins = msgText.split("\\s+");

                        for (int i = 0; i < coins.length; i++) {
                            coins[i] = convertSymbolName(coins[i]);
                            System.out.println(coins[i]);
                        }

                        filterUserCoins(coins);

                        System.out.println("\nAdded coins:\n\n");

                        String coinsNames = "";

                        for (String userCoin : userCoins) {
                            System.out.println(userCoin);
                            coinsNames += userCoin + "\n";
                        }

                        sendMsg(message, "Added coins:\n" + coinsNames);

                    } else {

                        msgText = convertSymbolName(msgText);

                        try {
                            sendMsg(message, Binance.getSymbolInfo(msgText, model));
                        } catch (IOException e) {
                            sendMsg(message, "Unknown symbol.");
                            e.printStackTrace();
                        }
                    }
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

    private static void filterUserCoins(String[] coins) {
        for (String coin : coins) {
            if (allCoins.contains(coin)) {
                if (!userCoins.contains(coin)) {
                    userCoins.add(coin);
                } else {
                    System.out.println("Coin \"" + coin + "\" is already exist in user's coins list.");
                }
            } else {
                System.out.println("Coin \"" + coin + "\" doesn't exist on BINANCE.");
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

        keyboardFirstRow.add(new KeyboardButton("/help"));
        keyboardFirstRow.add(new KeyboardButton("/settings"));

        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public String getBotUsername() {
        return "mycryptoinfomvc777bot";
    }

    public String getBotToken() {
        return "1340695166:AAGMWgwQFoKYSCaK8DObbkHdPx0KGJWynJk";
    }
}
