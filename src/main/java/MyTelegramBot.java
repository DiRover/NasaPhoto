import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.List;

public class MyTelegramBot extends TelegramLongPollingBot {
    public static final String BOT_TOKEN = "6866833690:AAH_8f0vjAAohzHjiD12vKoBMoPqTZMpd6k";

    public static final String BOT_USERNAME = "nasa_rover_bot";

    public static final String URI = "https://api.nasa.gov/planetary/apod?api_key=VeGwhZ4gAJsNgdgtfbFCFJhS6jg2UCALkr3TUdf2";

    public static String chatId;

    public static long userId;

    private boolean screaming = false;

    String emoji = "\uD83E\uDD28"; //U+1F624 u26BD

    private InlineKeyboardMarkup keyboardM1;
    private InlineKeyboardMarkup keyboardM2;

    public MyTelegramBot() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    private void scream(Long id, Message msg) {
        if(msg.hasText())
            sendText(id, msg.getText().toUpperCase());
        else
            copyMessage(id, msg.getMessageId());  //We can't really scream a sticker
    }

    @Override
    public void onUpdateReceived(Update update) {

//        if (!update.hasMessage()) return;

        var msg = update.getMessage();
        chatId = msg.getChatId().toString();
        var user = msg.getFrom();
        userId = user.getId();

        var next = InlineKeyboardButton.builder()
                .text("Next").callbackData("next")
                .build();

        var back = InlineKeyboardButton.builder()
                .text("Back").callbackData("back")
                .build();

        var url = InlineKeyboardButton.builder()
                .text("Tutorial")
                .url("https://core.telegram.org/bots/api")
                .build();

        keyboardM1 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(next)).build();

//Buttons are wrapped in lists since each keyboard is a set of button rows
        keyboardM2 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(back))
                .keyboardRow(List.of(url))
                .build();

        var txt = msg.getText();

        if(msg.isCommand()){
            if(msg.getText().equals("/scream"))         //If the command was /scream, we switch gears
                screaming = true;
            else if (msg.getText().equals("/whisper"))  //Otherwise, we return to normal
                screaming = false;
            else if (txt.equals("/menu"))
                sendMenu(userId, "<b>Menu 1</b>", keyboardM1);
            else if (txt.equals("/help"))
                sendMessage("Привет, я бот NASA! Я высылаю ссылки на картинки по запросу. " +
                        "Напоминаю, что картинки на сайте NASA обновляются раз в сутки");
            else if (txt.equals("/give"))
                try {
                    sendMessage(Utils.getUrl(URI)); //Utils.getUrl(URI)
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            else {
                sendMessage("Я не понимаю :( U+1F614");
            }
            return;                                     //We don't want to echo commands, so we exit
        }

        switch (update.getMessage().getText()) {
            case "/help":
                sendMessage("Привет, я бот NASA! Я высылаю ссылки на картинки по запросу. " +
                        "Напоминаю, что картинки на сайте NASA обновляются раз в сутки");
                break;
            case "/give":
                try {
                    sendMessage(Utils.getUrl(URI)); //Utils.getUrl(URI)
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                sendMessage("Я не понимаю! " + emoji);
        }

        if(screaming)                            //If we are screaming
            scream(userId, update.getMessage());     //Call a custom method
        else
//            copyMessage(userId, msg.getMessageId()); //Else proceed normally

        System.out.println(user.getFirstName() + " wrote " + msg.getText());
    }

    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(kb).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public void copyMessage(Long who, Integer msgId){
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
                .chatId(who.toString())      //And send it back to him
                .messageId(msgId)            //Specifying what message
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        System.out.println(messageText);
        message.setText(messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
