package se.ifmo.blazingzephyr.model;

/**
 * Типы организации.
 * @author blazingzephyr
 * @version 1.0
 */
public enum OrganizationType {
    GOVERNMENT,
    TRUST,
    PRIVATE_LIMITED_COMPANY,
    OPEN_JOINT_STOCK_COMPANY;

    /**
     * Выводит все возможные доступные типы организации.
     * @return Строки тип организации
     */
    public static String getTypes() {
        StringBuilder sb = new StringBuilder();
        for (OrganizationType type : values()) {
            sb.append(type.name()).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }
}
