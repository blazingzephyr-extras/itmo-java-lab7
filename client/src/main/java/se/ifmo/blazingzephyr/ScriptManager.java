package se.ifmo.blazingzephyr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.FileUtility;

public class ScriptManager {

    private static final Set<String> scriptStack = new HashSet<>();

    public static String execute(String filePath, CommandUtility commands) {

        if (scriptStack.contains(filePath)) {
            return "Ошибка: рекурсия! Файл " + filePath + " уже выполняется выше по стеку.";
        }

        StringBuilder scriptOutput = new StringBuilder("---" + filePath + "---\n");
        scriptStack.add(filePath);

        try (BufferedReader reader = FileUtility.ReadBuffer(filePath);
            Scanner scriptScanner = new Scanner(reader)) {

            while (scriptScanner.hasNextLine()) {
                String line = scriptScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                ValidationResult validation = commands.validate(line);

                if (validation.isError()) {
                    scriptOutput.append(validation.error().get().getMessage()).append('\n');
                    continue;
                }

                Request request = validation.request().get();

                if (
                    request.getCommandType() == CommandType.HISTORY
                    || request.getCommandType() == CommandType.EXIT
                    || request.getCommandType() == CommandType.HELP) {
                    scriptOutput.append("[").append(request.getCommandType().name().toLowerCase())
                                .append(" недоступна в скрипте]\n");
                    continue;
                }

                if (request.getCommandType() == CommandType.EXECUTE_SCRIPT) {
                    CommandPayload.WithScriptName payload = (CommandPayload.WithScriptName) request.getPayload();
                    scriptOutput.append(execute(payload.scriptName(), commands)).append('\n');
                    continue;
                }

                try {
                    Response response = App.sendRequest(request);
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