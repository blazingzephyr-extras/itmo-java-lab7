package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.stream.Collectors;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.TableUtility;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;

/**
 * Обращает порядок элементов в коллекции.
 * @author blazingzephyr
 * @version 1.0
 */
public class ReorderCommand implements Command<None> {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.REORDER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, None args, String login) {
        return String.format(
            """
            Порядок элементов в таблице реляционной базы данных не имеет значения.
            Соответственно, его нельзя обратить.
            Напечатаю коллекцию в обратном порядке.
            Количество организаций: %d%n%s%n%s
            """,
            ctx.collection().size(),
            TableUtility.getHeader(),
            ctx.collection()
                .stream()
                .sorted(Comparator.reverseOrder())
                .map(TableUtility::getEntry)
                .collect(Collectors.joining("\n"))
        );
    }
}
