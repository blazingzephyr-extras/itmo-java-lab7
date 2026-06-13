package se.ifmo.blazingzephyr.commands;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Response;

/**
 * Выводит справку о коллекции.
 * @author blazingzephyr
 * @version 1.0
 */
public class InfoCommand implements Command<None> {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.INFO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response execute(ServerContext ctx, None args, String login) {

        String creationTime;
        try {
            Optional<Timestamp> time = ctx.database().creationDate();
            if (time.isEmpty()) {
                creationTime = "???";
            }
            else {
                creationTime = String.valueOf(time.get());
            }
        } catch (SQLException ex) {
            creationTime = "???. " + ex.getMessage();
        }

        String collectionType;
        try {
            collectionType = "\n" + ctx
                .database()
                .getColumns()
                .stream()
                .map(a -> "\t[" + a + "]")
                .collect(Collectors.joining(";\n"));

        } catch (SQLException ex) {
            collectionType = "???. " + ex.getMessage();
        }

        String size;
        try {
            Optional<Long> count = ctx.database().count();
            if (count.isEmpty()) {
                size = "???";
            }
            else {
                size = String.valueOf(count.get());
            }
        } catch (SQLException ex) {
            size = "???. " + ex.getMessage();
        }
        
        return Response.ok(
            "info.success",
            collectionType,
            creationTime,
            size
        );
    }
}
