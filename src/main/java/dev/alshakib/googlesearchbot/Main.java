package dev.alshakib.googlesearchbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Robot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
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
