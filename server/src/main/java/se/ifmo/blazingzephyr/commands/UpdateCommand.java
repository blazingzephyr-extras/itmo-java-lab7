package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithIdAndOrganization;

/**
 * Обновляет элемент в коллекции.
 * @author blazingzephyr
 * @version 1.0
 */
public class UpdateCommand implements Command<WithIdAndOrganization> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.UPDATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, WithIdAndOrganization args) {

        long id = args.id();
        OrganizationData data = args.organization();

        try {
            boolean success = ctx.database().update(id, data);
            if (!success)
            {
                return "Организация не была обновлена. " +
                    "Проверьте существование организации с искомым ID.";
            }
            else
            {
                return String.format("Организация с ID %d успешно изменена.%n", id);
            }
        } catch (SQLException ex) {
            return "Произошла ошибка во время обновления объекта в базе данных: " + ex.getMessage();
        }
    }
}
