package se.ifmo.blazingzephyr.commands;

import java.sql.SQLException;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.None;

/**
 * Очищает коллекцию, уничтожая элементы.
 * @author blazingzephyr
 * @version 1.0
 */
public class ClearCommand implements Command<None> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.CLEAR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, None args) {
        try {
            ctx.database().deleteAll();
            ctx.collection().clear();
            return "Элементы коллекции были зачищены.\nПроверьте, используя show. Для дополнительных опций используйте help.";
        } catch (SQLException e) {
            return "Произошла ошибка во время очистки базы данных: " + e.getLocalizedMessage();
        }
    }
}
