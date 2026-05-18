package se.ifmo.blazingzephyr.commands;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.CommandPayload;
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

        Organization organization = new Organization(args.organization());
        ctx.collection().add(organization);
        
        return String.format(
            "Организация '%s' успешно добавлена с ID %d.",
            organization.getName(),
            organization.getId());
    }
}
