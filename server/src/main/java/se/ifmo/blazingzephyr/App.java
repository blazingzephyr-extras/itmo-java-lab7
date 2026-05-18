package se.ifmo.blazingzephyr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.Scanner;
import java.util.Stack;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.utility.FileUtility;

public class App {
    
    public static void main(String[] args) {
        DatabaseManager manager = new DatabaseManager();
        manager.connect();
    }

    public static void m_ain(String[] args) {

        String fp = System.getenv("DATA_SHEET_PATH");
        if (fp == null || fp.isEmpty()) {
            System.out.println("Отсутствует переменная среды пути файла! Выбрано стандартное значение.");
            fp = "data.csv";
        }

        Stack<Organization> temp = new Stack<>();
        try {
            temp = FileUtility.ReadDatabase(fp);
        } catch (FileNotFoundException ex) {
            System.out.println("Искомый файл базы данных не был найден: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            System.out.println("Возникла ошибка доступа к файла: " + ex.getLocalizedMessage());
        }

        final String fpath = fp;
        final Stack<Organization> collection = temp;

        // Сохранение коллекции в файл при завершении работы приложения.
        // Используем хук, чтобы сохранять коллекцию даже при незапланированном завершении сервера.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtility.WriteDatabase(fpath, collection);
            }
            catch (IOException e) {
                System.out.println("Возникла ошибка при записи базы данных в файл: " + e.getLocalizedMessage());
            }
        }));

        try {
            // Объявляем сервер.
            Server server = new Server(fpath, collection);

            // Запускаем консоль администратора в отдельном потоке.
            Thread consoleThread = new Thread(() -> runServerConsole(fpath, collection, server));
            consoleThread.setDaemon(true);
            consoleThread.start();

            // Запускаем основной сервер.
            server.run();
        }

        catch (IOException e) {
            System.out.println("Невозможно запустить сервер: " + e.getLocalizedMessage());
        }
    }

    // Запускаем серверные команды в отдельном потоке.
    private static void runServerConsole(String fpath, Stack<Organization> collection, Server server) {

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Серверные команды: save, exit");

            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine().trim();
                switch (cmd) {
                    case "save" -> {
                        try {
                            FileUtility.WriteDatabase(fpath, collection);
                            System.out.println("Коллекция сохранена в " + fpath);
                        }
                        catch (IOException e) {
                            System.err.println("Ошибка при сохранении: " + e.getMessage());
                        }
                    }
                    case "exit" -> {
                        System.out.println("Завершение сервера...");
                        server.stop();
                        return;
                    }
                    default -> System.out.println("Неизвестная команда. Доступны следующие команды: save, exit");
                }
            }
        }
    }
}
