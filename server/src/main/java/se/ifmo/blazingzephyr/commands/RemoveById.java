package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import java.util.Optional;

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
    public String execute(ServerContext ctx, WithId args, String login) throws IllegalArgumentException {

        long id = args.id();
        Optional<Organization> org;
        try {
            org = ctx.database().selectById(id);
        } catch (SQLException ex) {
            return "Произошла ошибка во время получения объекта из базы данных: " + ex.getMessage();
        }

        if (org.isEmpty())
        {
            return "Организации с искомым ID не существует.";
        }

        if (!org.get().getOwner().equals(login))
        {
            return "Невозможно удалить объект, не принадлежащий данному пользователю.";
        }

        try {


            boolean success = ctx.database().deleteById(id);
            if (!success)
            {
                return "Организация не была удалена.";
            }
            else
            {
                ctx.collection().remove(org.get());
                return String.format("Организация с ID %d успешно удалена.%n", id);
            }
        } catch (SQLException ex) {
            return "Произошла ошибка во время удаления объекта в базе данных: " + ex.getMessage();
        }
    }
}
