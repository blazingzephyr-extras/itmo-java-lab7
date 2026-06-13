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
            return Response.error("update.error_get", ex.getMessage());
        }

        if (org.isEmpty())
        {
            return Response.error("update.not_found");
        }

        if (!org.get().getOwner().equals(login))
        {
            return Response.error("update.not_owned");
        }

        try {

            boolean success = ctx.database().update(id, data);
            if (!success)
            {
                return Response.error("update.error_unknown");
            }
            else
            {
                Organization organization = ctx.collection().stream().filter(o -> o.getId() == id).findFirst().get();
                organization.setName(data.getName())
                    .setName(data.getName())
                    .setCoordinates(data.getCoordinates())
                    .setFullName(data.getFullName())
                    .setAnnualTurnover(data.getAnnualTurnover())
                    .setOrganizationType(data.getOrganizationType())
                    .setOfficialAddress(data.getOfficialAddress());

                return Response.ok("update.success", id);
            }
        } catch (SQLException ex) {
            return Response.error("update.error", ex.getMessage());
        }
    }
}
