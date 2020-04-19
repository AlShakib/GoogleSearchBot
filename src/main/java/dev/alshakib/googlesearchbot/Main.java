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
            String botToken = "";
            String botUsername = "";
            String adminUsername = "";
            long adminUserId = 0;
            boolean adminOnlyMode = false;
            if (System.getenv("TELEGRAM_BOT_API_TOKEN") != null) {
                botToken = System.getenv("TELEGRAM_BOT_API_TOKEN");
            }
            if (System.getenv("TELEGRAM_BOT_USERNAME") != null) {
                botUsername = System.getenv("TELEGRAM_BOT_USERNAME");
            }
            if (System.getenv("ADMIN_USERNAME") != null) {
                adminUsername = System.getenv("ADMIN_USERNAME");
            }
            if (System.getenv("ADMIN_USER_ID") != null) {
                adminUserId = Long.parseLong(System.getenv("ADMIN_USER_ID"));
            }
            if (System.getenv("ADMIN_ONLY_MODE") != null) {
                adminOnlyMode = Boolean.parseBoolean(System.getenv("ADMIN_ONLY_MODE"));
            }
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
