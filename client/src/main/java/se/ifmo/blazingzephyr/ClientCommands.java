package se.ifmo.blazingzephyr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithName;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;

public class ClientCommands {

    public static String printHistory(List<Request> history) {
        return String.format(
            "Последние 15 команд (исключая эту):\n%s",
            history.stream()
                .map(Request::getCommandType)
                .map(CommandType::name)
                .collect(Collectors.joining("\n"))
        );
    }

    public static String printHelp(CommandPayload payload, Map<String, CommandStub> commands) {
        StringBuilder builder = new StringBuilder();

        if (payload instanceof WithName name) {
            String cmd = name.name().toLowerCase();
            if (commands.containsKey(cmd)) {
                CommandStub command = commands.get(cmd);
                builder.append(String.format("%s %s\n- %s",
                    command.name(), command.getSyntax(), command.getDescription()));

                if (command.getArguments().length > 0) {
                    builder.append('\n');
                    for (String s : command.getArguments()) {
                        builder.append('\n').append(s);
                    }
                }
            }
        } else {
            builder.append("Список имеющихся команд:\n");
            for (CommandStub command : commands.values()) {
                builder.append(String.format(" - %s: %s\n", command.name(), command.getDescription()));
            }
        }

        return builder.toString();
    }

    public static String executeScript(Request request, CommandUtility commands) {
        CommandPayload.WithScriptName payload = (CommandPayload.WithScriptName) request.getPayload();
        return ScriptManager.execute(payload.scriptName(), commands);
    }
}
