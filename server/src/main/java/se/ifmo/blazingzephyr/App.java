package se.ifmo.blazingzephyr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class App {
    
    public static void main(String[] args) {

        // Подключаемся к БД PostgreSQL.
        DatabaseManager database;
        try {
            String url = "jdbc:postgresql://localhost:5432/";
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "admin");
            props.setProperty("ssl", "false");
            database = new DatabaseManager(url, props);
        }
        catch (SQLException ex) {
            System.out.println("Невозможно подключиться к PostgreSQL: " + ex.getMessage());
            return;
        }

        try {
            // Объявляем сервер.
            Server server = new Server(database);

            // Запускаем консоль администратора в отдельном потоке.
            Thread consoleThread = new Thread(() -> runServerConsole(server));
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
    private static void runServerConsole(Server server) {

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Серверные команды: exit");

            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine().trim();
                switch (cmd) {
                    // Команда 'save' на сервере больше не требуется.
                    // База данных сама сохраняет все изменения.
                    // case "save" -> {
                    //  ...
                    //  return;
                    // }

                    case "exit" -> {
                        System.out.println("Завершение сервера...");
                        server.stop();
                        return;
                    }
                    default -> System.out.println("Неизвестная команда. Доступны следующие команды: exit");
                }
            }
        }
    }
}
