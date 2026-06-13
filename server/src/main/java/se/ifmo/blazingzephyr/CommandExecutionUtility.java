package se.ifmo.blazingzephyr;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.ifmo.blazingzephyr.commands.*;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;
import se.ifmo.blazingzephyr.networking.Response;

public class CommandExecutionUtility {

    private static final Logger log = LogManager.getLogger(CommandExecutionUtility.class);

    private final Map<CommandType, Command> commands;

    public CommandExecutionUtility() {
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

        log.debug("CommandExecutionUtility инициализирован. Зарегистрировано команд: {}.", commands.size());
    }

    public Response execute(ServerContext ctx, Request request) {
        CommandType type = request.getCommandType();
        String login = request.getLogin();

        log.debug("Обработка запроса: команда={}, логин='{}'.", type, login);

        // Регистрация.
        if (type == CommandType.REGISTER) {
            log.info("Запрос на регистрацию нового пользователя: '{}'.", login);
            try {
                boolean ok = ctx.database().registerUser(login, request.getPassword());
                if (ok) {
                    log.info("Регистрация пользователя '{}' выполнена успешно.", login);
                    return Response.ok("Регистрация успешна.");
                } else {
                    log.warn("Регистрация отклонена: логин '{}' уже занят.", login);
                    return Response.error("Пользователь с таким логином уже существует.");
                }
            } catch (SQLException e) {
                log.error("Ошибка БД при регистрации пользователя '{}': {}", login, e.getMessage(), e);
                return Response.error("Ошибка БД: " + e.getMessage());
            }
        }

        // Авторизация пользователя.
        log.debug("Аутентификация пользователя '{}' для команды {}.", login, type);
        try {
            if (!ctx.database().authenticate(login, request.getPassword())) {
                log.warn("Отказ в аутентификации: неверные учётные данные для логина '{}'.", login);
                return Response.error("Неверный логин или пароль.");
            }
        } catch (SQLException e) {
            log.error("Ошибка БД при аутентификации пользователя '{}': {}", login, e.getMessage(), e);
            return Response.error("Ошибка БД при авторизации: " + e.getMessage());
        }

        // Диспетчеризация
        if (type == CommandType.AUTHORIZE) {
            log.info("Пользователь '{}' успешно авторизован.", login);
            return Response.ok("Успешно авторизовано.");
        } else if (!this.commands.containsKey(type)) {
            log.warn("Получена неизвестная команда '{}' от пользователя '{}'.", type, login);
            return Response.error("Такой команды не существует");
        } else {
            log.info("Выполнение команды {} для пользователя '{}'.", type, login);
            Response response = commands.get(type).execute(ctx, request.getPayload(), login);
            log.debug("Команда {} для пользователя '{}' завершена.", type, login);
            return response;
        }
    }
}
