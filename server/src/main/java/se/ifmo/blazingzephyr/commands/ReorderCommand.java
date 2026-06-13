package se.ifmo.blazingzephyr.commands;

import java.util.Comparator;
import java.util.stream.Collectors;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.TableUtility;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Response;
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
    public Response execute(ServerContext ctx, None args, String login) {
        return Response.ok(
            "reorder.message",
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
