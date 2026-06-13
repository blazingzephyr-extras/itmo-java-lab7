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
        "[--command: String]",
        new String[]
        {
            "args.command",
            "args.extra"
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
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.INFO);
        }
    ),

    SHOW(
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.SHOW);
        }
    ),

    ADD(
        "{--element: Organization}",
        new String[] { "element" },
        ctx -> {
            // Заместо чтения из консоли вызывается новый диалог для создания организаций.
            Optional<OrganizationData> data = App.showOrganizationDialog();
            if (data.isEmpty())
            {
                return ValidationResult.error(ValidationError.NO_ITEM);
            }

            return ValidationResult.ok(CommandType.ADD, new CommandPayload.WithOrganization(data.get()));
        }
    ),

    CLEAR(
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.CLEAR);
        }
    ),

    REORDER(
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.REORDER);
        }
    ),

    PRINT_FIELD_ASCENDING_ANNUAL_TURNOVER(
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.PRINT_FIELD_ASCENDING_ANNUAL_TURNOVER);
        }
    ),

    MIN_BY_NAME(
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.MIN_BY_NAME);
        }
    ),

    REMOVE_BY_ID(
        "[--id: long]",
        new String[] { "id" },
        ctx -> {
            long id_arg;
            if (ctx.args().length < 1) return ValidationResult.error(ValidationError.ID_NEEDED);
            if ((id_arg = Long.parseLong(ctx.args()[0])) < 0) return ValidationResult.error(ValidationError.ID_CANT_BE_NEGATIVE);
            return ValidationResult.ok(CommandType.REMOVE_BY_ID, new CommandPayload.WithId(id_arg));
        }
    ),

    FILTER_GREATER_THAN_TYPE(
        "type",
        new String[] { "type" },
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
        "id {--element: Organization}",
        new String[] {
            "id",
            "element"
        },
        ctx -> {
            long id_arg;
            if (ctx.args().length < 1) return ValidationResult.error(ValidationError.ID_NEEDED);
            if ((id_arg = Long.parseLong(ctx.args()[0])) < 0) return ValidationResult.error(ValidationError.ID_CANT_BE_NEGATIVE);

            // Заместо чтения из консоли вызывается новый диалог для создания организаций.
            Optional<OrganizationData> data = App.showOrganizationDialog();
            if (data.isEmpty())
            {
                return ValidationResult.error(ValidationError.NO_ITEM);
            }
            
            return ValidationResult.ok(CommandType.UPDATE, new CommandPayload.WithIdAndOrganization(id_arg, data.get()));
        }
    ),

    ADD_IF_MIN(
        "{--element: Organization}",
        new String[] { "element" },
        ctx -> {
            // Заместо чтения из консоли вызывается новый диалог для создания организаций.
            Optional<OrganizationData> data = App.showOrganizationDialog();
            if (data.isEmpty())
            {
                return ValidationResult.error(ValidationError.NO_ITEM);
            }

            return ValidationResult.ok(CommandType.ADD_IF_MIN, new CommandPayload.WithOrganization(data.get()));
        }
    ),
    
    EXECUTE_SCRIPT(
        "file_name",
        new String[] { "file_name" },
        ctx -> {
            
            if (ctx.args().length < 1) return ValidationResult.error(ValidationError.FILE_PATH_NEEDED);
            if (!Files.exists(Paths.get(ctx.args()[0]))) return ValidationResult.error(ValidationError.FILE_DOES_NOT_EXIST);
            return ValidationResult.ok(CommandType.EXECUTE_SCRIPT, new CommandPayload.WithScriptName(ctx.args()[0]));
        }
    ),
    
    HISTORY(
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.HISTORY);
        }
    ),
    
    EXIT(
        "",
        new String[] { },
        ctx -> {
            return ValidationResult.ok(CommandType.EXIT);
        }
    );

    private final String syntax;
    private final String[] arguments;
    private final Function<ValidationContext, ValidationResult> validation;
    
    CommandStub(String syntax, String[] arguments, Function<ValidationContext, ValidationResult> validation) {
        this.syntax = syntax;
        this.arguments = arguments;
        this.validation = validation;
    }

    public String getSyntax() { return this.syntax; }
    public String[] getArguments() { return this.arguments; }
    public Function<ValidationContext, ValidationResult> getValidator() { return this.validation; }
}
