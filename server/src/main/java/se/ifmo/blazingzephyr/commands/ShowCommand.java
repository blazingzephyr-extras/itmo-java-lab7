package se.ifmo.blazingzephyr.commands;

import java.util.stream.Collectors;
import se.ifmo.blazingzephyr.ServerContext;

import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;
import se.ifmo.blazingzephyr.TableUtility;

/**
 * Выводит коллекцию в удобный для пользователя вид.
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
