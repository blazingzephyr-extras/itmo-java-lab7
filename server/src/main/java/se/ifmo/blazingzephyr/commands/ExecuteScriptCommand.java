package se.ifmo.blazingzephyr.commands;

import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.CommandPayload.WithScriptName;
// import se.ifmo.blazingzephyr.ScriptManager;
import se.ifmo.blazingzephyr.ServerContext;

/**
 * Выполняет команды из файла.
 * @author blazingzephyr
 * @version 1.0
 */
public class ExecuteScriptCommand implements Command<WithScriptName> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.EXECUTE_SCRIPT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, WithScriptName args) {

        return "Не выполняется на сервере.";
        // return ScriptManager.execute(args.scriptName(), ctx);
    }
}
