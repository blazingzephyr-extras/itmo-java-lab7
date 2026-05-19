package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import java.util.Optional;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;
import se.ifmo.blazingzephyr.TableUtility;

/**
 * Выводит объект из коллекции, name которого является минимальным.
 * @author blazingzephyr
 * @version 1.0
 */
public class MinByName implements Command<None> {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.MIN_BY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, None args) {

        try {
            Optional<Organization> min = ctx.database().getMinByName();
            if (min.isEmpty()) {
                return "Элемента с минимальным значением name не существует.";
            }
            else {
                return String.format(
                    "Элемент с минимальным значением name.\n%s\n%s",
                    TableUtility.getHeader(),
                    TableUtility.getEntry(min.get()));
            }
        }
        catch (SQLException ex) {
            return "Произошла ошибка во время получения минимального элемента из базы данных. " + ex.getMessage();
        }
    }
}
