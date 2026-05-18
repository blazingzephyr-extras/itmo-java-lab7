package se.ifmo.blazingzephyr;

import java.text.SimpleDateFormat;
import se.ifmo.blazingzephyr.model.Organization;

/**
 * Утилита, содержащая в себе бойлерплейт код для табличного вывода коллекции.
 * @author blazingzephyr
 * @version 1.0
 */
public class TableUtility {

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    static String header = String.format("| %-13s | %-10s | %-8s | %-8s | %-20s | %-10s | %-25s | %-24s | %-20s | %-9s |", 
        "ID", "Название", "X", "Y", "Дата создания", "Выручка", "Полное наименование", "Тип", "Улица", "Почт. код") + "\n" +
        "|---------------|------------|----------|----------|----------------------|" +
        "------------|---------------------------|--------------------------|----------------------|-----------|";

    /**
     * Выводит в стандартный поток вывода заголовок таблицы.
     * @return заголовок таблицы (повторяющийся элемент).
     */
    public static String getHeader() {
        return header;
    }

    /**
     * Выводит в стандартный поток вывода табличный элемент, соответствующий организации.
     * @param org Организация, вывести данные которой требуется.
     * @return Отформатированная запись организации.
     */
    public static String getEntry(Organization org) {

        String entry = String.format("| %-13s | %-10s | %-8s | %-8s | %-20s | %-10s | %-25s | %-24s | %-20s | %-9s |", 
            org.getId(),
            org.getName(),
            org.getCoordinates().getX(),
            org.getCoordinates().getY(),
            dateFormat.format(org.getCreationDate()),
            org.getAnnualTurnover(),
            org.getFullName(),
            org.getOrganizationType(),
            org.getOfficialAddress().getStreet(),
            org.getOfficialAddress().getZipCode());

        return entry;
    }
}
