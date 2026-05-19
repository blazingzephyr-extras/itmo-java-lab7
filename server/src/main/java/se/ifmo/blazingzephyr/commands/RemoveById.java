package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithId;
import se.ifmo.blazingzephyr.TableUtility;

/**
 * Удаляет объект из коллекции по ID.
 * @author blazingzephyr
 * @version 1.0
 */
public class RemoveById implements Command<WithId> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.REMOVE_BY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, WithId args) throws IllegalArgumentException {

        long id = args.id();
        try {
            boolean success = ctx.database().deleteById(id);
            if (!success)
            {
                return "Организация не была удалена. " +
                    "Проверьте существование организации с искомым ID.";
            }
            else
            {
                return String.format("Организация с ID %d успешно удалена.%n", id);
            }
        } catch (SQLException ex) {
            return "Произошла ошибка во время удаления объекта в базе данных: " + ex.getMessage();
        }
    }
}
