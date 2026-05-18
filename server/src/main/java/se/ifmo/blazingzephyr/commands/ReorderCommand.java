package se.ifmo.blazingzephyr.commands;

import java.util.Collections;
import se.ifmo.blazingzephyr.ServerContext;

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
    public String execute(ServerContext ctx, None args) {
        Collections.reverse(ctx.collection());
        return """
            Порядок элементов коллекции был обращён.
            Проверьте, используя show. Для дополнительных опций вызовите help.
            """;
    }
}
