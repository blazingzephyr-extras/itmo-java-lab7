package se.ifmo.blazingzephyr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Управляет исполнением команд.
 * @author blazingzephyr
 * @version 1.0
 */
public class CommandUtility {
    
    private final Map<String, CommandStub> commands;
    public CommandUtility() {
        this.commands = new HashMap<>();
        for (CommandStub stub : CommandStub.values()) {
            commands.put(stub.name().toLowerCase(), stub);
        }
    }

    public Map<String, CommandStub> getCommands() {
        return this.commands;
    }

    /**
     * Исполняет введённые пользователем команды.
     * @param input Пользовательский ввод, который требуется обработать.
     * @param context Контекст, в котором исполняется команда.
     * @return Возвращает результат исполнения программы.
     * @throws Exception Ошибка исполнения команды.
     */
    public ValidationResult validate(String input, Scanner scanner)
    {
        String[] parts = input.trim().split(" ");
        String cmd = parts[0].toLowerCase();
        String[] arguments = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[] {};

        if (!commands.containsKey(cmd)) {
            
            return ValidationResult.error(ValidationError.NO_COMMAND);
        }
        else {
            CommandStub command = commands.get(cmd);
            return command.getValidator().apply(new ValidationContext(scanner, arguments));
        }
    }
}
