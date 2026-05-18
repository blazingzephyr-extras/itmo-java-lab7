package se.ifmo.blazingzephyr.commands;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithOrganization;
import se.ifmo.blazingzephyr.networking.CommandType;

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
        return CommandType.ADD_IF_MAX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, WithOrganization arg) {

        Organization min = ctx.collection().stream().min(Organization::compareTo).get();
        Organization organization = new Organization(arg.organization());

        if (organization.compareTo(min) < 0)
        {
            ctx.collection().add(organization);
            return String.format(
                "Организация '%s' успешно добавлена с ID %d.",
                organization.getName(),
                organization.getId());
        }
        else {
            return "Организация не была добавлена.";
        }
    }
}
