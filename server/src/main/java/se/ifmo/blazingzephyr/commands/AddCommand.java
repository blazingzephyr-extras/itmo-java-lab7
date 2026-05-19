package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithOrganization;

/**
 * Добавляет элемент в коллекцию.
 * @author blazingzephyr
 * @version 1.0
 */
public class AddCommand implements Command<WithOrganization> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.ADD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, WithOrganization args) {

        OrganizationData data = args.organization();
        try {
            long id = ctx.database().insert(data);
            return String.format(
                "Организация '%s' успешно добавлена с ID %d.",
                data.getName(),
                id);
        } catch (SQLException e) {
            return "Произошла ошибка во время добавления объекта в базу данных: " + e.getLocalizedMessage();
        }
    }
}
