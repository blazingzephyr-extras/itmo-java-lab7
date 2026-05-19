
package se.ifmo.blazingzephyr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import se.ifmo.blazingzephyr.commands.*;
import se.ifmo.blazingzephyr.model.*;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;

public class CommandExecutionUtility {
    
    private final Map<CommandType, Command> commands;

    public CommandExecutionUtility()
    {
        this.commands = Stream.of(
            new InfoCommand(),
            new ShowCommand(),
            new AddCommand(),
            new ClearCommand(),
            new ReorderCommand(),
            new PrintFieldAscendingAnnualTurnoverCommand(),
            new MinByName(),
            new RemoveById(),
            new FilterGreaterThanTypeCommand(),
            new UpdateCommand(),
            new AddIfMinCommand(),
            new ExecuteScriptCommand()
        )
        .collect(Collectors.toMap(Command::getType, Function.identity()));
    }

    public Response execute(ServerContext ctx, Request request) {
        // Регистрация.
        if (request.getCommandType() == CommandType.REGISTER) {
            try {
                boolean ok = ctx.database().registerUser(request.getLogin(), request.getPassword());
                return ok
                    ? Response.ok("Регистрация успешна.")
                    : Response.error("Пользователь с таким логином уже существует.");
            } catch (SQLException e) {
                return Response.error("Ошибка БД: " + e.getMessage());
            }
        }

        // Авторизация пользователя.
        try {
            if (!ctx.database().authenticate(request.getLogin(), request.getPassword())) {
                return Response.error("Неверный логин или пароль.");
            }
        } catch (SQLException e) {
            return Response.error("Ошибка БД при авторизации: " + e.getMessage());
        }

        // Диспетчеризация
        CommandType type = request.getCommandType();
        if (!this.commands.containsKey(type)) {

            return Response.error("Такой команды не существует");
        }
        else {
            String output = commands.get(type).execute(ctx, request.getPayload(), request.getLogin());
            return Response.ok(output);
        }
    }
}
