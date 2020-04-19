package dev.alshakib.googlesearchbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private String adminUsername;
    private long adminUserId;
    private boolean adminOnlyMode;

    public Bot(String botToken, String botUsername) {
        botToken = botToken.trim();
        botUsername = botUsername.trim();
        if (botToken.isEmpty()) {
            throw new IllegalArgumentException("Bot Token is empty");
        }
        if (botUsername.isEmpty()) {
            throw new IllegalArgumentException("Bot Username is empty");
        }
        this.botToken = botToken;
        this.botUsername = extractUsername(botUsername);
    }

    public Bot(String botToken, String botUsername, long adminUserId, boolean adminOnlyMode) {
        this(botToken, botUsername);
        if (adminOnlyMode) {
            if (adminUserId == 0) {
                throw new IllegalArgumentException("ADMIN_ONLY_MODE is set but ADMIN_USER_ID is not set");
            }
        }
        this.adminUserId = adminUserId;
        this.adminOnlyMode = adminOnlyMode;
    }

    public Bot(String botToken, String botUsername, String adminUsername, long adminUserId, boolean adminOnlyMode) {
        this(botToken, botUsername, adminUserId, adminOnlyMode);
        this.adminUsername = extractUsername(adminUsername);
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = extractUsername(adminUsername);
    }

    public void setAdminUserId(long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public void setAdminOnlyMode(boolean adminOnlyMode) {
        this.adminOnlyMode = adminOnlyMode;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public long getAdminUserId() {
        return adminUserId;
    }

    public boolean isAdminOnlyMode() {
        return adminOnlyMode;
    }

    private String extractUsername(String username) {
        if (username != null) {
            username = username.trim();
            if (username.startsWith("@")) {
                return username.substring(1);
            }
            return username;
        }
        return "";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasInlineQuery()) {
            try {
                handleIncomingInlineQuery(update.getInlineQuery());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage()) {
            try {
                handleIncomingMessage(update.getMessage());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleIncomingInlineQuery(InlineQuery inlineQuery) throws TelegramApiException {
        String query = inlineQuery.getQuery().trim();
        if (!query.isEmpty()) {
            if (adminOnlyMode) {
                if (inlineQuery.getFrom().getId() == adminUserId) {
                    fetchGoogleSearch(inlineQuery);
                } else {
                    InputTextMessageContent messageContent = new InputTextMessageContent();
                    messageContent.enableHtml(true);
                    messageContent.setMessageText(getUnauthorizedMessage());
                    InlineQueryResultArticle unauthorizedArticle = new InlineQueryResultArticle();
                    unauthorizedArticle.setInputMessageContent(messageContent);
                    unauthorizedArticle.setId("UnauthorizedAccess");
                    unauthorizedArticle.setTitle("You are not authorized to access this bot");
                    unauthorizedArticle.setDescription("For further information please contact @" + adminUsername);
                    AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
                    answerInlineQuery.setInlineQueryId(inlineQuery.getId());
                    answerInlineQuery.setResults(unauthorizedArticle);
                    execute(answerInlineQuery);
                }
            } else {
                fetchGoogleSearch(inlineQuery);
            }
        }
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        if (adminOnlyMode) {
            if (message.getFrom().getId() == adminUserId) {
                sendIntroMessage(message);
            } else {
                SendMessage unauthorizedMessage = new SendMessage();
                unauthorizedMessage.setChatId(message.getChatId());
                unauthorizedMessage.setParseMode("HTML");
                unauthorizedMessage.setText(getUnauthorizedMessage());
                execute(unauthorizedMessage);
            }
        } else {
            sendIntroMessage(message);
        }
    }

    private void fetchGoogleSearch(InlineQuery inlineQuery) throws TelegramApiException {
        GoogleSearch googleSearch = new GoogleSearch(inlineQuery);
        ArrayList<InlineQueryResult> results = googleSearch.getSearchResults();
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(inlineQuery.getId());
        answerInlineQuery.setResults(results);
        execute(answerInlineQuery);
    }

    private void sendIntroMessage(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setParseMode("HTML");
        sendMessage.setText("To search the Web type <code>@" + getBotUsername() +
                " something</code> in the message field.");
        InlineKeyboardMarkup searchNowInlineKeyboard = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button = new ArrayList<>();
        button.add(new InlineKeyboardButton().setText("Search now").setSwitchInlineQuery(""));
        buttons.add(button);
        searchNowInlineKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(searchNowInlineKeyboard);
        execute(sendMessage);
    }

    private String getUnauthorizedMessage() {
        return "<b>You are not authorized to access this bot.</b>\n\n" +
                "For further information please contact @" + adminUsername + "\n" +
                "However you may host this bot by yourself.\n" +
                "For more information about self hosting, please visit\n" +
                "https://gitlab.com/AlShakib/GoogleSearchBot";
    }
}
