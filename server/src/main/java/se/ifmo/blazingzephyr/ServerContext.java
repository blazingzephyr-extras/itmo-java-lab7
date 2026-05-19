package se.ifmo.blazingzephyr;

import java.util.List;
import se.ifmo.blazingzephyr.model.Organization;

/**
 * Data-transfer-only объект.
 * Причина, по которой он не был удалён: чтобы не передавать в Command.execute изменяющийся набор аргументов.
 * 
 * Stack<Organization> был заменён на DatabaseManager.
 * Путь к CSV-файлу удалён по причине ненадобности.
 * 
 * @param collection Объект базы данных.
 * @param manager Менеджер базы данных.
 * 
 * @author blazingzephyr
 * @version 1.0
 */
public record ServerContext(List<Organization> collection, DatabaseManager database) { }
