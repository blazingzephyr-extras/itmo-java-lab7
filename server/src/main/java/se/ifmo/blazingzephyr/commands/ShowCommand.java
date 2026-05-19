package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import java.util.Stack;
import java.util.stream.Collectors;
import se.ifmo.blazingzephyr.ServerContext;

import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;
import se.ifmo.blazingzephyr.TableUtility;
import se.ifmo.blazingzephyr.model.Organization;
/**
 * Команда для вывода всех элементов коллекции.
 * @author blazingzephyr
 * @version 1.0
 */
public class ShowCommand implements Command<None> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.SHOW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, None args) {

        if (ctx.collection().isEmpty()) {
            return "Коллекция пуста.";
        }

        return String.format(
            "Количество элементов: %d%n%s%n%s",
            ctx.collection().size(),
            TableUtility.getHeader(),
            ctx.collection()
                .stream()
                .map(TableUtility::getEntry)
                .collect(Collectors.joining("\n"))
        );
    }
}
