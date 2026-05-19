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

import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.Serializer;

public class Server {

    private final ServerContext context;
    private final DatagramChannel channel;

    // AtomicBoolean вместо volatile boolean -- неблокирующий и потокобезопасный из java.util.concurrent.
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final ForkJoinPool requestPool;
    private final ForkJoinPool processPool;

    public Server(DatabaseManager database) throws IOException, SQLException {
        List<Organization> collection = database.selectAll();
        List<Organization> synchronizedCollection = Collections.synchronizedList(collection);

        this.context = new ServerContext(synchronizedCollection, database);

        this.channel = DatagramChannel.open();
        channel.configureBlocking(true);
        channel.bind(new InetSocketAddress(2100));

        this.requestPool = new ForkJoinPool();
        this.processPool = new ForkJoinPool();
    }

    public void run() {
        isRunning.set(true);

        // Запускаем несколько задач-читателей в requestPool,
        // каждая из которых независимо ждёт пакета.
        int readerCount = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < readerCount; i++) {
            requestPool.submit(new ReaderTask());
        }

        // Основной поток ожидает остановки сервера.
        while (isRunning.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        requestPool.shutdown();
        processPool.shutdown();
    }

    public void stop() {
        isRunning.set(false);
        try {
            channel.close();
        } catch (IOException e) {
            System.err.println("Ошибка закрытия канала: " + e.getMessage());
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
            } catch (IOException e) {
                System.err.println("Ошибка отправки: " + e.getMessage());
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
                SocketAddress clientAddress = channel.receive(buffer);
                if (clientAddress == null) {
                    // Канал закрыт — не перезапускаем задачу
                    return;
                }

                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                Request request = Serializer.deserialize(data);

                // Перезапускаем задачу-читатель до начала обработки,
                // чтобы следующий пакет читался параллельно с обработкой текущего.
                if (isRunning.get()) {
                    requestPool.submit(new ReaderTask());
                }

                // Обработка запроса в processPool
                processPool.submit(() -> {
                    CommandExecutionUtility commandUtility = new CommandExecutionUtility();
                    try {
                        Response response = commandUtility.execute(context, request);
                        sendResponse(response, clientAddress);
                    } catch (Exception e) {
                        System.err.println("Ошибка обработки запроса: " + e.getMessage());
                        sendResponse(Response.error("Ошибка сервера: " + e.getMessage()), clientAddress);
                    }
                });

            }

            catch (IOException | ClassNotFoundException ex) {
                if (!isRunning.get()) return;
                System.err.println("Ошибка чтения: " + ex.getMessage());

                // При ошибке чтения перезапускаем читатель, чтобы не потерять поток
                if (isRunning.get()) {
                    requestPool.submit(new ReaderTask());
                }
            }
        }
    }
}
