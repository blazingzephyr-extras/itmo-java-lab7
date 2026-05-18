package se.ifmo.blazingzephyr.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import se.ifmo.blazingzephyr.ServerContext;

import se.ifmo.blazingzephyr.networking.CommandPayload.None;
import se.ifmo.blazingzephyr.networking.CommandType;

/**
 * Выводит справку о коллекции.
 * @author blazingzephyr
 * @version 1.0
 */
public class InfoCommand implements Command<None> {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommandType getType() {
        return CommandType.INFO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(ServerContext ctx, None args) {

        String creationTime;
        try {
            BasicFileAttributes file = Files.readAttributes(Path.of(ctx.filepath()), BasicFileAttributes.class);
            creationTime = file.creationTime().toString();
        } catch (IOException e) {
            creationTime = "невозможно получить данные о дате создания файла";
        }

        String collectionType = "невозможно определить тип коллекции, отсутствуют элементы";
        if (!ctx.collection().isEmpty()) {
            collectionType = ctx.collection().get(0).getClass().getCanonicalName();
        }
        
        return String.format(
            "Тип элементов коллекции: %s\nДата инициализации: %s\nКоличество элементов: %d",
            collectionType,
            creationTime,
            ctx.collection().size()
        );
    }
}
