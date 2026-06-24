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
                    return Response.ok("register.success");
                } else {
                    log.warn("Регистрация отклонена: логин '{}' уже занят.", login);
                    return Response.error("register.login_exists");
                }
            } catch (SQLException e) {
                log.error("Ошибка БД при регистрации пользователя '{}': {}", login, e.getMessage(), e);
                return Response.error("register.bd_error");
            }
        }
        // Регистрация нового root-пароля.
        else if (type == CommandType.UPDATE_ROOT_PASSWORD) {
            log.info("Запрос на сохранение нового root-пароля: '{}'.", login);
            try {
                boolean ok = ctx.database().setRootPassword(request.getPassword());
                if (ok) {
                    log.info("Сохранение нового пароля '{}' выполнено успешно.", login);
                    return Response.ok("change.password.correct");
                } else {
                    log.warn("Не удалось сохранить новый пароль.", login);
                    return Response.error("change.password.server_error");
                }
            } catch (SQLException e) {
                log.error("Ошибка БД при сохранении нового пароля root-пользователя '{}': {}", login, e.getMessage(), e);
                return Response.error("register.bd_error");
            }
        }

        // Авторизация пользователя.
        log.debug("Аутентификация пользователя '{}' для команды {}.", login, type);
        try {
            if (!ctx.database().authenticate(login, request.getPassword())) {
                log.warn("Отказ в аутентификации: неверные учётные данные для логина '{}'.", login);
                return Response.error("register.incorrect_login");
            }
        } catch (SQLException e) {
            log.error("Ошибка БД при аутентификации пользователя '{}': {}", login, e.getMessage(), e);
            return Response.error("register.bd_error");
        }

        // Диспетчеризация
        if (type == CommandType.AUTHORIZE) {
            log.info("Пользователь '{}' успешно авторизован.", login);
            if (login.equals("root") && request.getPassword().equals("root")) {
                log.warn("Необходимо изменить пароль root-пользователя!");
                return Response.ok("register.auth_root_initial");
            }
            return Response.ok("register.auth_success");
        }
        // Команды управления группами — только для root.
        else if (type == CommandType.GET_USERS
                || type == CommandType.GET_GROUPS
                || type == CommandType.GET_RELATIONS
                || type == CommandType.CREATE_GROUP
                || type == CommandType.ADD_USER_TO_GROUP
                || type == CommandType.REMOVE_USER_FROM_GROUP
                || type == CommandType.DELETE_GROUP) {

            if (!"root".equals(login)) {
                log.warn("Доступ запрещён: команда {} требует root, получена от '{}'.", type, login);
                return Response.error("groups.access_denied");
            }

            try {
                return switch (type) {
                    case GET_USERS -> {
                        log.info("root запрашивает список пользователей.");
                        yield Response.okList("groups.users", ctx.database().selectAllUsers());
                    }
                    case GET_GROUPS -> {
                        log.info("root запрашивает список групп.");
                        yield Response.okList("groups.groups", ctx.database().selectAllGroups());
                    }
                    case GET_RELATIONS -> {
                        log.info("root запрашивает список членств в группах.");
                        yield Response.okList("groups.relations", ctx.database().selectAllGroupRelations());
                    }
                    case CREATE_GROUP -> {
                        String groupName = (String) ((se.ifmo.blazingzephyr.networking.CommandPayload.StringArg) request.getPayload()).value();
                        log.info("root создаёт группу '{}'.", groupName);
                        var created = ctx.database().createGroup(groupName);
                        yield created.isPresent()
                                ? Response.ok("groups.created", created.get())
                                : Response.error("groups.name_exists");
                    }
                    case ADD_USER_TO_GROUP -> {
                        var p = (se.ifmo.blazingzephyr.networking.CommandPayload.TwoLongs) request.getPayload();
                        log.info("root добавляет пользователя ID={} в группу ID={}.", p.first(), p.second());
                        boolean added = ctx.database().addUserToGroup(p.first(), p.second());
                        yield added ? Response.ok("groups.user_added") : Response.error("groups.already_member");
                    }
                    case REMOVE_USER_FROM_GROUP -> {
                        var p = (se.ifmo.blazingzephyr.networking.CommandPayload.TwoLongs) request.getPayload();
                        log.info("root удаляет пользователя ID={} из группы ID={}.", p.first(), p.second());
                        boolean removed = ctx.database().removeUserFromGroup(p.first(), p.second());
                        yield removed ? Response.ok("groups.user_removed") : Response.error("groups.not_member");
                    }
                    case DELETE_GROUP -> {
                        long groupId = ((se.ifmo.blazingzephyr.networking.CommandPayload.LongArg) request.getPayload()).value();
                        log.info("root удаляет группу ID={}.", groupId);
                        boolean deleted = ctx.database().deleteGroup(groupId);
                        yield deleted ? Response.ok("groups.deleted") : Response.error("groups.not_found");
                    }
                    default -> Response.error("no_such_command");
                };
            } catch (SQLException e) {
                log.error("Ошибка БД при выполнении команды {}: {}", type, e.getMessage(), e);
                return Response.error("register.bd_error");
            }
        }
        else if (!this.commands.containsKey(type)) {
            log.warn("Получена неизвестная команда '{}' от пользователя '{}'.", type, login);
            return Response.error("no_such_command");
        } else {
            log.info("Выполнение команды {} для пользователя '{}'.", type, login);
            Response response = commands.get(type).execute(ctx, request.getPayload(), login);
            log.debug("Команда {} для пользователя '{}' завершена.", type, login);
            return response;
        }
    }
}
