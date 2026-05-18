package se.ifmo.blazingzephyr.commands;

import java.util.stream.Collectors;
import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;

/**
 * Выводит значения поля annualTurnover всех элементов в порядке возрастания.
 * @author blazingzephyr
 * @version 1.0
 */
public class PrintFieldAscendingAnnualTurnoverCommand implements Command<None> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.PRINT_FIELD_ASCENDING_ANNUAL_TURNOVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, None args) {

        return String.format(
            "| %-40s |\n|------------------------------------------|\n%s",
            "Годовая выручка в порядке возрастания.",
            ctx.collection().stream()
                .map(Organization::getAnnualTurnover)
                .sorted(Double::compareTo)
                .map(t -> String.format("| %-40.2f | ", t))
                .collect(Collectors.joining("\n"))
        );
    }
}
