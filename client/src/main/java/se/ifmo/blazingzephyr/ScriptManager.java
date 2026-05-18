package se.ifmo.blazingzephyr;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.FileUtility;
import se.ifmo.blazingzephyr.utility.Serializer;

public class ScriptManager {

    private static final Set<String> scriptStack = new HashSet<>();

    /**
     * @param filePath  путь к скрипту
     * @param socket    уже открытый сокет клиента — тот же, что в App.java
     * @param address   адрес сервера
     * @param port      порт сервера
     * @param commands  CommandUtility — тот же экземпляр, что в App.java
     * @param history   история запросов клиента — тот же список, что в App.java
     */
    public static String execute(
            String filePath,
            DatagramSocket socket,
            InetAddress address,
            int port,
            CommandUtility commands) {

        // Защита от рекурсии
        if (scriptStack.contains(filePath)) {
            return "Ошибка: рекурсия! Файл " + filePath + " уже выполняется выше по стеку.";
        }

        StringBuilder scriptOutput = new StringBuilder("---" + filePath + "---\n");
        scriptStack.add(filePath);

        try (BufferedReader reader = FileUtility.ReadBuffer(filePath)) {
            Scanner scriptScanner = new Scanner(reader);

            while (scriptScanner.hasNextLine()) {
                String line = scriptScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                // Валидируем строку скрипта через тот же CommandUtility
                ValidationResult validation = commands.validate(line, scriptScanner);

                if (validation.isError()) {
                    // Ошибка валидации — выводим и продолжаем следующую строку
                    scriptOutput.append(validation.error().get().getMessage()).append('\n');
                    continue;
                }

                Request request = validation.request().get();

                // exit в скрипте — прерываем выполнение скрипта, не завершаем клиент
                if (request.getCommandType() == CommandType.EXIT) {
                    break;
                }

                // history и help — чисто клиентские, обрабатываем локально
                if (request.getCommandType() == CommandType.HISTORY) {
                    scriptOutput.append("[history недоступна в скрипте]\n");
                    continue;
                }
                if (request.getCommandType() == CommandType.HELP) {
                    scriptOutput.append("[help недоступна в скрипте]\n");
                    continue;
                }

                // execute_script — рекурсивный вызов, ScriptManager сам защищает от цикла
                if (request.getCommandType() == CommandType.EXECUTE_SCRIPT) {
                    CommandPayload.WithScriptName payload =
                        (CommandPayload.WithScriptName) request.getPayload();
                    String nested = execute(payload.scriptName(), socket, address, port, commands);
                    scriptOutput.append(nested).append('\n');
                    continue;
                }

                // Все остальные команды — отправляем на сервер, как в App.java
                try {
                    byte[] buffer = Serializer.serialize(request);
                    DatagramPacket requestDatagram =
                        new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(requestDatagram);

                    buffer = new byte[3000];
                    DatagramPacket responseDatagram =
                        new DatagramPacket(buffer, buffer.length);
                    socket.receive(responseDatagram);

                    Response response = Serializer.deserialize(responseDatagram.getData());
                    scriptOutput.append(response.getMessage()).append('\n');

                } catch (IOException | ClassNotFoundException e) {
                    scriptOutput.append("Ошибка сети: ").append(e.getMessage()).append('\n');
                }
            }

        } catch (IOException e) {
            scriptStack.remove(filePath);
            return "Ошибка чтения скрипта '" + filePath + "': " + e.getMessage();
        }

        scriptStack.remove(filePath);
        return scriptOutput.append("---").append(filePath).append(" завершён---").toString();
    }
}