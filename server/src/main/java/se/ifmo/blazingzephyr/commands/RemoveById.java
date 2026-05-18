package se.ifmo.blazingzephyr.commands;

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
    public String execute(ServerContext ctx, WithId args) throws IllegalArgumentException {

        java.util.Optional<Organization> org = ctx
            .collection()
            .stream()
            .filter(o -> o.getId() == args.id())
            .findFirst();

        if (org.isEmpty())
        {
            throw new IllegalArgumentException("Организации с искомым ID не существует!");
        }
        else
        {
            ctx.collection().remove(org.get());
            return String.format("%s%n%s%nЭлемент с ID %d успешно удалён.",
                TableUtility.getHeader(), 
                TableUtility.getEntry(org.get()),
                args.id()
            );
        }
    }
}
