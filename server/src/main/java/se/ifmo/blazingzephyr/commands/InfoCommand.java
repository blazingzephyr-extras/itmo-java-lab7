package se.ifmo.blazingzephyr.commands;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;
import se.ifmo.blazingzephyr.networking.CommandType;

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
    public String execute(ServerContext ctx, None args) {

        String creationTime;
        try {
            Optional<Timestamp> time = ctx.database().creationDate();
            if (time.isEmpty()) {
                creationTime = "невозможно определить.";
            }
            else {
                creationTime = String.valueOf(time.get());
            }
        } catch (SQLException ex) {
            creationTime = "невозможно определить. " + ex.getMessage();
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
            collectionType = "невозможно определить. " + ex.getMessage();
        }

        String size;
        try {
            Optional<Long> count = ctx.database().count();
            if (count.isEmpty()) {
                size = "невозможно определить.";
            }
            else {
                size = String.valueOf(count.get());
            }
        } catch (SQLException ex) {
            size = "невозможно определить количество элементов. " + ex.getMessage();
        }
        
        return String.format(
            "Тип элементов коллекции: %s\nДата инициализации: %s\nКоличество элементов: %s",
            collectionType,
            creationTime,
            size
        );
    }
}
