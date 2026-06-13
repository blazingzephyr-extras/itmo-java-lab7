package se.ifmo.blazingzephyr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;
import se.ifmo.blazingzephyr.i18n.LocaleManager;

import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.utility.FileUtility;

public class ScriptManager {

    private static final Set<String> scriptStack = new HashSet<>();

    public static String execute(String filePath, CommandUtility commands, LocaleManager lm) {

        if (scriptStack.contains(filePath)) {
            return String.format(lm.get("script_manager.recursion"), filePath);
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
                    String message = lm.get(String.valueOf(validation.error().get()).toLowerCase());
                    scriptOutput.append(message).append('\n');
                    continue;
                }

                Request request = validation.request().get();

                if (
                    request.getCommandType() == CommandType.HISTORY
                    || request.getCommandType() == CommandType.EXIT
                    || request.getCommandType() == CommandType.HELP) {
                    scriptOutput.append("[").append(request.getCommandType().name().toLowerCase())
                                .append(lm.get("script_manager.unavailable"));
                    continue;
                }

                if (request.getCommandType() == CommandType.EXECUTE_SCRIPT) {
                    CommandPayload.WithScriptName payload = (CommandPayload.WithScriptName) request.getPayload();
                    scriptOutput.append(execute(payload.scriptName(), commands, lm)).append('\n');
                    continue;
                }

                try {
                    Response response = App.sendRequest(request);
                    String key = response.getMessage();
                    scriptOutput.append(String.format(lm.get(key), response.getArgs().toArray())).append('\n');
                } catch (IOException | ClassNotFoundException e) {
                    scriptOutput.append(String.format(lm.get("script_manager.network_error"), e.getMessage()));
                }
            }

        } catch (IOException e) {
            scriptStack.remove(filePath);
            return String.format(
                lm.get("script_manager.error_reading_script"),
                filePath,
                e.getMessage()
            );
        }

        scriptStack.remove(filePath);
        return scriptOutput.append("---").append(filePath).append("---").toString();
    }
}