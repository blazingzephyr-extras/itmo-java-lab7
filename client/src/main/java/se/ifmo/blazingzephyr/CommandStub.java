package se.ifmo.blazingzephyr;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import se.ifmo.blazingzephyr.model.OrganizationType;
import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.model.OrganizationData;

public enum CommandStub {

    HELP(
        "Выводит справку по доступным командам.",
        "[--command: String]",
        new String[]
        {
            "command: Команда, информацию о которой требуется узнать.",
            "   Аргумент можно опустить для получения информации о всех командах."
        },
        ctx -> {
                
            if (ctx.args().length < 1) {
                return ValidationResult.ok(CommandType.HELP);
            }
            else {
                return ValidationResult.ok(CommandType.HELP, new CommandPayload.WithName(ctx.args()[0]));
            }
        }
    ),

    INFO(
        "Выводит в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.).",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.INFO);
        }
    ),

    SHOW(
        "Выводит в стандартный поток вывода все элементы коллекции в строковом представлении.",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.SHOW);
        }
    ),

    ADD(
        "Добавляет новый элемент в коллекцию.",
        "{--element: Organization}",
        new String[] { "element: Элемент, который требуется добавить в коллекцию." },
        ctx -> {
            OrganizationData data = new OrganizationData().query(ctx.scanner());
            return ValidationResult.ok(CommandType.ADD, new CommandPayload.WithOrganization(data));
        }
    ),

    CLEAR(
        "Очищает коллекцию.",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.CLEAR);
        }
    ),

    REORDER(
        "Отсортировывает коллекцию в порядке, обратном нынешнему.",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.REORDER);
        }
    ),

    PRINT_FIELD_ASCENDING_ANNUAL_TURNOVER(
        "Выводит значения поля annualTurnover всех элементов в порядке возрастания",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.PRINT_FIELD_ASCENDING_ANNUAL_TURNOVER);
        }
    ),

    MIN_BY_NAME(
        "Выводит любой объект из коллекции, значение поля name которого является минимальным.",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.MIN_BY_NAME);
        }
    ),

    REMOVE_BY_ID(
        "Удаляет элемент из коллекции по его id.",
        "[--id: long]",
        new String[] { "id: ID элемента, который требуется удалить." },
        ctx -> {
            long id_arg;
            if (ctx.args().length < 1) return ValidationResult.error(ValidationError.ID_NEEDED);
            if ((id_arg = Long.parseLong(ctx.args()[0])) < 0) return ValidationResult.error(ValidationError.ID_CANT_BE_NEGATIVE);
            return ValidationResult.ok(CommandType.REMOVE_BY_ID, new CommandPayload.WithId(id_arg));
        }
    ),

    FILTER_GREATER_THAN_TYPE(
        "Выводит элементы, значение поля type которых больше заданного.",
        "type",
        new String[] { "type: Значение поля type [" + OrganizationType.getTypes() + "]" },
        ctx -> {
            if (ctx.args().length < 1) return ValidationResult.error(ValidationError.TYPE_NEEDED);
            try {
                OrganizationType targetType = OrganizationType.valueOf(ctx.args()[0].toUpperCase());
                return ValidationResult.ok(CommandType.FILTER_GREATER_THAN_TYPE, new CommandPayload.WithOrganizationType(targetType));
            }
            catch (Exception e) { return ValidationResult.error(ValidationError.NO_SUCH_TYPE); }
        }
    ),

    UPDATE(
        "Обновляет значение элемента коллекции, id которого равен заданному.",
        "id {--element: Organization}",
        new String[] {
            "id: ID элемента, который требуется изменить",
            "element: Новые параметры данного элемента."
        },
        ctx -> {
            long id_arg;
            if (ctx.args().length < 1) return ValidationResult.error(ValidationError.ID_NEEDED);
            if ((id_arg = Long.parseLong(ctx.args()[0])) < 0) return ValidationResult.error(ValidationError.ID_CANT_BE_NEGATIVE);

            OrganizationData data = new OrganizationData().query(ctx.scanner());
            return ValidationResult.ok(CommandType.UPDATE, new CommandPayload.WithIdAndOrganization(id_arg, data));
        }
    ),

    ADD_IF_MIN(
        "Добавляет новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.",
        "{--element: Organization}",
        new String[] { "element: Элемент, который требуется добавить в коллекцию." },
        ctx -> {
            OrganizationData data = new OrganizationData().query(ctx.scanner());
            return ValidationResult.ok(CommandType.ADD, new CommandPayload.WithOrganization(data));
        }
    ),
    
    EXECUTE_SCRIPT(
        "Считывает и исполняет скрипт из указанного файла. " + 
            "В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.",
        "file_name",
        new String[] { "file_name: Файл, из которого требуется выполнить команду." },
        ctx -> {
            
            if (ctx.args().length < 1) return ValidationResult.error(ValidationError.FILE_PATH_NEEDED);
            if (!Files.exists(Paths.get(ctx.args()[0]))) return ValidationResult.error(ValidationError.FILE_DOES_NOT_EXIST);
            return ValidationResult.ok(CommandType.EXECUTE_SCRIPT, new CommandPayload.WithScriptName(ctx.args()[0]));
        }
    ),
    
    HISTORY(
        "Выводит последние 15 команд (без их аргументов).",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.HISTORY);
        }
    ),
    
    EXIT(
        "Завершает программу (без сохранения в файл).",
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.EXIT);
        }
    );

    private final String description;
    private final String syntax;
    private final String[] arguments;
    private final Function<ValidationContext, ValidationResult> validation;
    
    CommandStub(String description, String syntax, String[] arguments, Function<ValidationContext, ValidationResult> validation) {
        this.description = description;
        this.syntax = syntax;
        this.arguments = arguments;
        this.validation = validation;
    }

    public String getDescription() { return this.description; }
    public String getSyntax() { return this.syntax; }
    public String[] getArguments() { return this.arguments; }
    public Function<ValidationContext, ValidationResult> getValidator() { return this.validation; }
}
