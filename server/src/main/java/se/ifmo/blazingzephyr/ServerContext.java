package se.ifmo.blazingzephyr;

/**
 * Data-transfer-only объект.
 * Причина, по которой он не был удалён: чтобы не передавать в Command.execute изменяющийся набор аргументов.
 * 
 * Stack<Organization> был заменён на DatabaseManager.
 * Путь к CSV-файлу удалён по причине ненадобности.
 * 
 * @param manager Менеджер базы данных.
 * 
 * @author blazingzephyr
 * @version 1.0
 */
public record ServerContext(DatabaseManager database) { }
