package dev.alshakib.googlesearchbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            String botToken = System.getenv("TELEGRAM_BOT_API_TOKEN");
            String botUsername = System.getenv("TELEGRAM_BOT_USERNAME");
            String adminUsername = System.getenv("ADMIN_USERNAME");
            long adminUserId = 0;
            if (System.getenv("ADMIN_USER_ID") != null) {
                adminUserId = Long.parseLong(System.getenv("ADMIN_USER_ID"));
            }
            boolean adminOnlyMode = Boolean.parseBoolean(System.getenv("ADMIN_ONLY_MODE"));
            Bot googleSearchBot = new Bot(botToken, botUsername, adminUsername, adminUserId, adminOnlyMode);
            telegramBotsApi.registerBot(googleSearchBot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (System.getenv("PORT") != null) {
            try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(System.getenv("PORT")))) {
                while (true) {
                    serverSocket.accept();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Google Search Bot started!");
    }
}
