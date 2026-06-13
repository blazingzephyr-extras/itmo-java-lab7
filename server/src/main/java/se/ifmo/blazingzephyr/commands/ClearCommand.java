package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;

/**
 * Очищает коллекцию, уничтожая элементы.
 * @author blazingzephyr
 * @version 1.0
 */
public class ClearCommand implements Command<None> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.CLEAR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response execute(ServerContext ctx, None args, String login) {
        try {
            ctx.database().deleteAll(login);
            ctx.collection().removeIf(o -> o.getOwner().equals(login));
            return Response.ok("clear.success");
        } catch (SQLException e) {
            return Response.error(
                "clear.error",
                e.getMessage()
            );
        }
    }
}
