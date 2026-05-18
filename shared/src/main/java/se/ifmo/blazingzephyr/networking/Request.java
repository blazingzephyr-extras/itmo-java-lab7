package se.ifmo.blazingzephyr.networking;

import java.io.Serial;
import java.io.Serializable;

/**
 * Объект запроса — летит от клиента к серверу по UDP (сериализованный).
 * Несёт тип команды и типобезопасный payload.
 */
public final class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final CommandType commandType;
    private final CommandPayload payload;

    public Request(CommandType commandType, CommandPayload payload) {
        this.commandType = commandType;
        this.payload = payload;
    }

    public Request(CommandType commandType) {
        this(commandType, new CommandPayload.None());
    }

    public CommandType getCommandType() { return this.commandType; }
    public CommandPayload getPayload() { return this.payload; }

    @Override
    public String toString() {
        return "Request{command=" + this.commandType + ", payload=" + this.payload + "}";
    }
}