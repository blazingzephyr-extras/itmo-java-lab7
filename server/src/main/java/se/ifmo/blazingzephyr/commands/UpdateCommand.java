package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import java.util.Optional;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Response;
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
    public Response execute(ServerContext ctx, WithIdAndOrganization args, String login) {

        long id = args.id();
        OrganizationData data = args.organization();

        Optional<Organization> org;
        try {
            org = ctx.database().selectById(id);
        } catch (SQLException ex) {
            return Response.error("Произошла ошибка во время получения объекта из базы данных: " + ex.getMessage());
        }

        if (org.isEmpty())
        {
            return Response.error("Организации с искомым ID не существует.");
        }

        if (!org.get().getOwner().equals(login))
        {
            return Response.error("Невозможно изменить объект, не принадлежащий данному пользователю.");
        }

        try {

            boolean success = ctx.database().update(id, data);
            if (!success)
            {
                return Response.error("Организация не была обновлена. " +
                    "Проверьте существование организации с искомым ID.");
            }
            else
            {
                Organization organization = org.get();
                organization.setName(data.getName())
                    .setName(data.getName())
                    .setCoordinates(data.getCoordinates())
                    .setFullName(data.getFullName())
                    .setAnnualTurnover(data.getAnnualTurnover())
                    .setOrganizationType(data.getOrganizationType())
                    .setOfficialAddress(data.getOfficialAddress());

                return Response.ok(String.format("Организация с ID %d успешно изменена.%n", id));
            }
        } catch (SQLException ex) {
            return Response.error("Произошла ошибка во время обновления объекта в базе данных: " + ex.getMessage());
        }
    }
}
