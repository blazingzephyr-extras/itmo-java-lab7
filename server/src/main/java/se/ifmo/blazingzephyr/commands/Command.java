package se.ifmo.blazingzephyr.commands;

import se.ifmo.blazingzephyr.ServerContext;
import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;

/**
 * Интерфейс команды.
 * @author blazingzephyr
 * @version 1.0
 */
public interface Command<T extends CommandPayload> extends Cloneable {
    /**
     * Возвращает тип команды.
     * @return Тип команды для обмена с клиентами.
     */
    CommandType getType();

    /**
     * Исполняет команду.
     * @param context Контекст исполнения (дополнительные данные комманды).
     * @param payload Аргументы команды.
     */
    String execute(ServerContext context, T payload);
}
