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

public class Robot extends TelegramLongPollingBot {
    private final String ADMIN_USERNAME = System.getenv("ADMIN_USERNAME");
    private final long ADMIN_USER_ID = Integer.parseInt(System.getenv("ADMIN_USER_ID"));
    private final boolean ADMIN_ONLY_MODE = Boolean.parseBoolean(System.getenv("ADMIN_ONLY_MODE"));

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_API_TOKEN");
    }

    @Override
    public String getBotUsername() {
        return System.getenv("TELEGRAM_BOT_USERNAME");
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
            if (ADMIN_ONLY_MODE) {
                if (inlineQuery.getFrom().getId() == ADMIN_USER_ID || inlineQuery.getFrom()
                        .getUserName().equals(ADMIN_USERNAME)) {
                    fetchDuckDuckGoQuery(inlineQuery);
                } else {
                    InputTextMessageContent messageContent = new InputTextMessageContent();
                    messageContent.enableHtml(true);
                    messageContent.setMessageText("<b>You are not authorized to access this bot.</b>\n\n" +
                            "For further information please contact @" + ADMIN_USERNAME);
                    InlineQueryResultArticle unauthorizedArticle = new InlineQueryResultArticle();
                    unauthorizedArticle.setInputMessageContent(messageContent);
                    unauthorizedArticle.setId("UnauthorizedAccess");
                    unauthorizedArticle.setTitle("You are not authorized to access this bot");
                    unauthorizedArticle.setDescription("For further information please contact @" + ADMIN_USERNAME);
                    AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
                    answerInlineQuery.setInlineQueryId(inlineQuery.getId());
                    answerInlineQuery.setResults(unauthorizedArticle);
                    execute(answerInlineQuery);
                }
            } else {
                fetchDuckDuckGoQuery(inlineQuery);
            }
        }
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        if (ADMIN_ONLY_MODE) {
            if (message.getFrom().getId() == ADMIN_USER_ID || message.getFrom()
                    .getUserName().equals(ADMIN_USERNAME)) {
                sendIntroMessage(message);
            } else {
                SendMessage unauthorizedMessage = new SendMessage();
                unauthorizedMessage.setChatId(message.getChatId());
                unauthorizedMessage.setParseMode("HTML");
                unauthorizedMessage.setText("<b>You are not authorized to access this bot.</b>\n\n" +
                        "For further information please contact @" + ADMIN_USERNAME);
                execute(unauthorizedMessage);
            }
        } else {
            sendIntroMessage(message);
        }
    }

    private void fetchDuckDuckGoQuery(InlineQuery inlineQuery) throws TelegramApiException {
        GoogleSearch duckDuckGoQuery = new GoogleSearch(inlineQuery);
        ArrayList<InlineQueryResult> results = duckDuckGoQuery.getSearchResults();
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
}
