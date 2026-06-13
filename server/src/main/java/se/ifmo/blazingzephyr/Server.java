package se.ifmo.blazingzephyr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.Serializer;

public class Server {

    private static final Logger log = LogManager.getLogger(Server.class);

    private final ServerContext context;
    private final DatagramChannel channel;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final ForkJoinPool requestPool;
    private final ForkJoinPool processPool;

    public Server(DatabaseManager database) throws IOException, SQLException {
        log.debug("Загрузка коллекции организаций из БД...");
        List<Organization> collection = database.selectAll();
        log.info("Загружено {} организаций из БД.", collection.size());

        List<Organization> synchronizedCollection = Collections.synchronizedList(collection);
        this.context = new ServerContext(synchronizedCollection, database);

        this.channel = DatagramChannel.open();
        channel.configureBlocking(true);
        channel.bind(new InetSocketAddress(2100));
        log.info("UDP-канал открыт и привязан к порту 2100.");

        this.requestPool = new ForkJoinPool();
        this.processPool = new ForkJoinPool();
        log.debug("Пулы потоков инициализированы: requestPool и processPool.");
    }

    public void run() {
        isRunning.set(true);

        int readerCount = Runtime.getRuntime().availableProcessors();
        log.info("Запуск {} задач-читателей (по числу доступных процессоров).", readerCount);

        for (int i = 0; i < readerCount; i++) {
            requestPool.submit(new ReaderTask());
        }

        log.info("Сервер запущен и ожидает входящих запросов.");

        // Основной поток ожидает остановки сервера.
        while (isRunning.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Основной поток прерван.", e);
                break;
            }
        }

        log.info("Основной цикл завершён. Останавливаем пулы потоков...");
        requestPool.shutdown();
        processPool.shutdown();
        log.info("Пулы потоков остановлены.");
    }

    public void stop() {
        log.info("Получен сигнал остановки сервера.");
        isRunning.set(false);
        try {
            channel.close();
            log.info("UDP-канал закрыт.");
        } catch (IOException e) {
            log.error("Ошибка при закрытии UDP-канала: {}", e.getMessage(), e);
        }
    }

    // Общая логика отправки.
    private void sendResponse(Response response, SocketAddress clientAddress) {
        new Thread(() -> {
            try {
                byte[] bytes = Serializer.serialize(response);
                ByteBuffer sendBuf = ByteBuffer.wrap(bytes);
                synchronized (channel) {
                    channel.send(sendBuf, clientAddress);
                }
                log.debug("Ответ отправлен клиенту {}: {} байт.", clientAddress, bytes.length);
            } catch (IOException e) {
                log.error("Ошибка отправки ответа клиенту {}: {}", clientAddress, e.getMessage(), e);
            }
        }).start();
    }

    /**
     * Задача чтения одного UDP-пакета. После получения пакета
     * перезапускает саму себя в пуле — так в requestPool всегда работает
     * readerCount параллельных читателей.
     */
    private class ReaderTask extends RecursiveAction {

        @Override
        protected void compute() {
            if (!isRunning.get()) return;

            // Буфер создаётся на каждый пакет отдельно,
            // исключая гонку данных между итерациями.
            ByteBuffer buffer = ByteBuffer.allocate(65507);

            try {
                log.trace("Задача-читатель ожидает входящего пакета...");
                SocketAddress clientAddress = channel.receive(buffer);

                if (clientAddress == null) {
                    log.debug("channel.receive() вернул null — канал закрыт. Задача-читатель завершается.");
                    return;
                }

                log.info("Получен новый UDP-пакет от клиента: {}", clientAddress);

                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                Request request = Serializer.deserialize(data);

                log.debug("Десериализован запрос от {}: команда={}, логин={}",
                        clientAddress, request.getCommandType(), request.getLogin());

                // Перезапускаем задачу-читатель до начала обработки.
                if (isRunning.get()) {
                    requestPool.submit(new ReaderTask());
                    log.trace("Новая задача-читатель запущена в requestPool.");
                }

                // Обработка запроса в processPool
                processPool.submit(() -> {
                    log.debug("Начало обработки запроса от {}: команда={}",
                            clientAddress, request.getCommandType());

                    CommandExecutionUtility commandUtility = new CommandExecutionUtility();
                    try {
                        Response response = commandUtility.execute(context, request);
                        log.info("Запрос от {} обработан успешно. Команда={}.",
                                clientAddress, request.getCommandType());
                        sendResponse(response, clientAddress);
                    } catch (Exception e) {
                        log.error("Ошибка обработки запроса от {}: {}", clientAddress, e.getMessage(), e);
                        sendResponse(Response.error("Ошибка сервера: " + e.getMessage()), clientAddress);
                    }
                });

            } catch (IOException | ClassNotFoundException ex) {
                if (!isRunning.get()) return;
                log.error("Ошибка чтения входящего пакета: {}", ex.getMessage(), ex);

                // При ошибке чтения перезапускаем читатель, чтобы не потерять поток.
                if (isRunning.get()) {
                    requestPool.submit(new ReaderTask());
                    log.debug("Задача-читатель перезапущена после ошибки.");
                }
            }
        }
    }
}
