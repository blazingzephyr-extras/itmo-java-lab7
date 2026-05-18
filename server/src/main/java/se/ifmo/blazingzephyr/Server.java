package se.ifmo.blazingzephyr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Stack;

import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.Serializer;

public class Server {

    private final Stack<Organization> collection;
    private final ServerContext context;
    private final DatagramChannel channel;
    private final ByteBuffer buffer;
    private boolean isRunning;

    public Server(String fpath, Stack<Organization> collection) throws IOException {
        this.collection = collection;
        this.context = new ServerContext(fpath, collection);
        this.isRunning = false;

        // Резервируем примерно 3000 байтов под сообщение пользователя.
        // Нужно помнить, что здесь будет приходить полная коллекция.
        this.buffer = ByteBuffer.allocate(3000);

        // Открываем канал датаграм и связываем его с портом 2100.
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(new InetSocketAddress(2100));
    }

    public void run() {

        // Ставим флажок запуска сервера.
        this.isRunning = true;

        CommandExecutionUtility commandUtility = new CommandExecutionUtility();
        try {
            while (isRunning) {

                buffer.clear();
                SocketAddress clientAddress = channel.receive(buffer);
                if (clientAddress == null) {

                    Thread.sleep(10);
                    continue;
                }

                Request request = Serializer.deserialize(buffer.array());
                Response response = commandUtility.execute(context, request);
                byte[] responseBuffer = Serializer.serialize(response);

                buffer.clear();
                buffer.put(responseBuffer);
                buffer.flip();
                channel.send(buffer, clientAddress);
            }
        }

        // Обработка не найденного класса.
        catch (ClassNotFoundException e) {
            System.out.println("Произошла ошибка при десериализации данных.\n" + 
                "Следующий класс не был найден: " + e.getLocalizedMessage());
        }

        // Обработки ошибки сокета.
        catch (SocketException e) {
            System.out.println("Произошла ошибка сети: " + e.getLocalizedMessage());
        }

        // Обрабатываем ошибки получения пакетов.
        catch (IOException e) {
            System.out.println("Произошла ошибка ввода-вывода: " + e.getLocalizedMessage());
        }
        
        // Обрабатываем ошибки прерывания.
        catch (InterruptedException e) {
            System.out.println("Сервер был прерван: " + e.getLocalizedMessage());
        }

        finally {
            System.out.println("Сервер был остановлен.");
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}
