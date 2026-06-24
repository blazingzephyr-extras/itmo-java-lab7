package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;
import java.util.Optional;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Response;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithId;

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
    public Response execute(ServerContext ctx, WithId args, String login) throws IllegalArgumentException {

        long id = args.id();
        Optional<Organization> org;
        try {
            org = ctx.database().selectById(id);
        } catch (SQLException ex) {
            return Response.error(
                "remove_by_id.error_get",
                ex.getMessage()
            );
        }

        if (org.isEmpty())
        {
            return Response.ok("remove_by_id.not_found");
        }

        // if (!org.get().getOwner().equals(login) && !login.equals("root"))
        // {
        //    return Response.error("remove_by_id.not_owned");
        // }

        try {
            boolean success = ctx.database().deleteById(id, login);
            if (!success)
            {
                return Response.ok("remove_by_id.not_deleted");
            }
            else
            {
                boolean removeFromDb = ctx.collection().removeIf(o -> o.getId() == id);
                if (!removeFromDb)
                {
                    return Response.ok("remove_by_id.partial_suc", id);
                }
                else
                {
                    return Response.ok("remove_by_id.success", id);
                }
            }
        } catch (SQLException ex) {
            return Response.error(
                "remove_by_id.error_del",
                ex.getMessage()
            );
        }
    }
}
