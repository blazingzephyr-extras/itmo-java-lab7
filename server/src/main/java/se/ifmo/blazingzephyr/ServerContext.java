package se.ifmo.blazingzephyr;

import java.util.Stack;
import se.ifmo.blazingzephyr.model.Organization;

/**
 * Data-transfer-only объект.
 * Причина, по которой он не был удалён: чтобы не передавать в Command.execute изменяющийся набор аргументов.
 * 
 * @param filepath Путь к файлу БД, из которого ведётся чтение и в который ведётся запись.
 * @param collection Объект базы данных.
 * 
 * @author blazingzephyr
 * @version 1.0
 */
public record ServerContext(String filepath, Stack<Organization> collection) { }
