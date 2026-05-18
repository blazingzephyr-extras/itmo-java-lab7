package se.ifmo.blazingzephyr.commands;

import java.util.stream.Collectors;
import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.OrganizationType;
import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithOrganizationType;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.TableUtility;

/**
 * Выводит элементы коллекции, чей тип больше, чем искомый.
 * @author blazingzephyr
 * @version 1.0
 */
public class FilterGreaterThanTypeCommand implements Command<WithOrganizationType> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.FILTER_GREATER_THAN_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, WithOrganizationType args) {
        
        String result = ctx.collection().stream()
                .filter(org -> org.getOrganizationType() != null && org.getOrganizationType().compareTo(args.organizationType()) > 0)
                .map(TableUtility::getEntry)
                .collect(Collectors.joining("\n"));

        if (result.isEmpty()) {
            return "Элементов с типом больше " + args.organizationType() + " не найдено.";
        }

        return String.format("Элементы с типом, больше чем %s:%n%s%n%s",
            args.organizationType(),
            TableUtility.getHeader(),
            result);
    }
}
