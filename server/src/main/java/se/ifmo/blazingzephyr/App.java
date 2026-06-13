package se.ifmo.blazingzephyr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

    private static final Logger log = LogManager.getLogger(App.class);

    public static void main(String[] args) {

        log.info("Запуск приложения...");

        // Подключаемся к БД PostgreSQL.
        DatabaseManager database;
        try {
            String url = "jdbc:postgresql://localhost:5432/";
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "admin");
            props.setProperty("ssl", "false");

            log.info("Подключение к PostgreSQL по адресу: {}", url);
            database = new DatabaseManager(url, props);
            log.info("Подключение к PostgreSQL установлено успешно.");
        }
        catch (SQLException ex) {
            log.fatal("Невозможно подключиться к PostgreSQL: {}", ex.getMessage(), ex);
            return;
        }

        try {
            log.info("Инициализация сервера...");
            Server server = new Server(database);

            // Запускаем консоль администратора в отдельном потоке.
            Thread consoleThread = new Thread(() -> runServerConsole(server));
            consoleThread.setDaemon(true);
            consoleThread.start();
            log.debug("Поток консоли администратора запущен.");

            // Запускаем основной сервер.
            log.info("Сервер запускается.");
            server.run();
        }
        catch (IOException e) {
            log.fatal("Невозможно запустить сервер: {}", e.getLocalizedMessage(), e);
        }
        catch (SQLException e) {
            log.fatal("Невозможно получить список объектов БД при запуске сервера: {}", e.getLocalizedMessage(), e);
        }

        log.info("Приложение завершило работу.");
    }

    private static void runServerConsole(Server server) {

        try (Scanner scanner = new Scanner(System.in)) {
            log.info("Консоль администратора готова. Доступные команды: exit");

            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine().trim();
                log.debug("Получена серверная команда: '{}'", cmd);

                switch (cmd) {
                    case "exit" -> {
                        log.info("Получена команда 'exit'. Остановка сервера...");
                        server.stop();
                        return;
                    }
                    default -> {
                        log.warn("Неизвестная серверная команда: '{}'", cmd);
                    }
                }
            }
        }
    }
}
