package se.ifmo.blazingzephyr;

public enum ValidationError {
    /**
     * Данной команды не существует.
     */
    NO_COMMAND("Данной команды не существует."),
    /**
     * Недостаточно аргументов. Требуется указать ID.
     */
    ID_NEEDED("Недостаточно аргументов. Требуется указать ID."),
    /**
     * ID элемента не может быть отрицательным.
     */
    ID_CANT_BE_NEGATIVE("ID элемента не может быть отрицательным."),
    /**
     * Не указан тип для сравнения.
     */
    TYPE_NEEDED("Не указан тип для сравнения."),
    /**
     * Такого типа организации не существует.
     */
    NO_SUCH_TYPE("Такого типа организации не существует."),
    /**
     * Не указан путь к файлу скрипта.
     */
    FILE_PATH_NEEDED("Не указан путь к файлу скрипта."),
    /**
     * Искомый файл скрипта не существует.
     */
    FILE_DOES_NOT_EXIST("Искомый файл скрипта не существует.");

    private final String message;
    ValidationError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
