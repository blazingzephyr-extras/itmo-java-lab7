package se.ifmo.blazingzephyr.commands;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
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
        java.util.Optional<Organization> org = ctx
            .collection()
            .stream()
            .filter(o -> o.getId() == args.id())
            .findFirst();

        if (org.isEmpty())
        {
            return "Организации с искомым ID не существует.";
        }
        else
        {
            Organization organization = org.get();
            OrganizationData data = args.organization();
            organization.setName(data.getName())
                .setName(data.getName())
                .setCoordinates(data.getCoordinates())
                .setFullName(data.getFullName())
                .setAnnualTurnover(data.getAnnualTurnover())
                .setOrganizationType(data.getOrganizationType())
                .setOfficialAddress(data.getOfficialAddress());

            return String.format("Организация с ID %d успешно изменена.%n", organization.getId());
        }
    }
}
