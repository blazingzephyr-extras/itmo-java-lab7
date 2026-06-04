package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import java.util.Optional;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithOrganization;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Response;

/**
 * Добавляет элемент в коллекцию.
 * @author blazingzephyr
 * @version 1.0
 */
public class AddIfMinCommand implements Command<CommandPayload.WithOrganization> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.ADD_IF_MIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response execute(ServerContext ctx, WithOrganization arg, String login) {

        Optional<Organization> min = Optional.empty();
        try {
            min = ctx.database().getMinByName();
        }
        catch (SQLException ex) {
            return Response.error("Произошла ошибка во время получения минимального элемента из базы данных");
        }

        OrganizationData data = arg.organization();
        if (min.isEmpty() || data.getName().compareTo(min.get().getName()) < 0)
        {
            try {
                Organization newOrg = ctx.database().insert(data, login);
                ctx.collection().add(newOrg);
                return Response.ok(String.format(
                    "Организация '%s' успешно добавлена с ID %d.",
                    data.getName(),
                    newOrg.getId()));
            } catch (SQLException e) {
                return Response.error("Произошла ошибка во время добавления объекта в базу данных: " + e.getLocalizedMessage());
            }
        }
        else {
            return Response.ok("Организация не была добавлена, потому что имеется минимальный элемент.");
        }
    }
}
