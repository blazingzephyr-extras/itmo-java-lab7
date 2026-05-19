package se.ifmo.blazingzephyr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.Serializer;

public class App {

    public static void main(String[] args) {

        // Используем try с ресурсами для открытия сокета для порта 2100.
        // Также используем его для открытия байтовых потоков.
        try (DatagramSocket socket = new DatagramSocket()) {

            // Утилиты для обработки команд.
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

            // Адрес и порт локалхоста для доступа к серверу.
            InetAddress address = InetAddress.getLocalHost();
            int port = 2100;

            // Утилита для валидации пользовательских команд.
            CommandUtility commands = new CommandUtility();
            ArrayList<Request> history = new ArrayList<>();

            // Проверка регистрации пользователя.
            System.out.println("Вы уже зарегистрированы (y/any)?");
            String answer = scanner.nextLine().trim().toLowerCase();

            // Регистрация нового пользователя.
            if (!answer.equals("y") && !RegistrationUtility.register(socket, address, port, scanner)) {
                return;
            }
            else {
                System.out.println();
            }

            // Сериализация объекта.
            byte[] buffer;
            while (true) {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                // Осуществляет валидацию введённого пользователем текста.
                ValidationResult validation = commands.validate(input, scanner);

                if (validation.isError()) {
                    System.out.println(validation.error().get().getMessage());
                    System.out.println();
                    continue;
                }

                Request request = validation.request().get();

                // Выводит историю команд конкретного клиента.
                if (request.getCommandType() == CommandType.HISTORY) { ClientCommands.printHistory(history); }

                // Help выводит в справке команды конкретного клиента.
                else if (request.getCommandType() == CommandType.HELP) { ClientCommands.printHelp(request.getPayload(), commands.getCommands()); }

                // Запрос на введение логина и пароля.
                System.out.print("Логин: ");
                String login = scanner.nextLine().trim();
                System.out.print("Пароль: ");
                String password = scanner.nextLine().trim();
                request.packAuthorization(login, password);

                // Выполнение скрипта.
                if (request.getCommandType() == CommandType.EXECUTE_SCRIPT) { ClientCommands.executeScript(request, socket, address, port, commands); }
                
                else if (request.getCommandType() == CommandType.EXIT) { return; }
                else {
                    // Сериализуем команды.
                    buffer = Serializer.serialize(request);

                    // Отправляет команду серверу.
                    DatagramPacket requestDatagram = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(requestDatagram);
    
                    // Получает ответ сервера и выводит его.
                    buffer = new byte[3000];
                    DatagramPacket responseDatagram = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responseDatagram);

                    Response response = Serializer.deserialize(responseDatagram.getData());
                    if (response.isSuccess()) {
                        history.add(request);
                    }

                    String message = new String(response.getMessage());
                    System.out.println(message);
                    System.out.println();
                }
            }
        }

        // Обработки ошибки сокета.
        catch (SocketException e) {
            System.out.println("Ошибка сокета: " + e.getLocalizedMessage());
        }

        // Обрабатываем ошибки получения пакетов.
        catch (IOException e) {
            System.out.println("Ошибка получения пакета: " + e.getLocalizedMessage());
        }
        
        // Обрабатываем ошибки неизвестного класса.
        catch (ClassNotFoundException e) {
            System.out.println("Ошибка десериализации: " + e.getLocalizedMessage());
        }
    }
}
