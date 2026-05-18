package se.ifmo.blazingzephyr.model;

import java.io.Serializable;

/**
 * Адрес организации.
 * @author blazingzephyr
 * @version 1.0
 */
public final class Address implements Serializable {
    private String street;  // Поле не может быть null
    private String zipCode; // Поле может быть null

    /**
     * Пустой конструктор адреса.
     */
    public Address() {
        this.street = "<MISSING>";
        this.zipCode = null;
    }

    /**
     * Возвращает адрес улицы.
     * @return строка, содержащая адрес улицы.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Устанавливает новое значение адресу улицы.
     * @param street улица (не может быть null).
     * @return true, если улице успешно присвоено значение, иначе false.
     */
    public Address setStreet(String street) {
        if (street == null)
        {
            throw new IllegalArgumentException("Значение 'street' не может быть null.");
        }

        this.street = street;
        return this;
    }

    /**
     * Возвращает почтовый код.
     * @return строка, обозначающая почтовый код.
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Устанавливает новое значение почтовому коду, присвоенному адресу.
     * @param zipCode почтовый код (может быть null).
     */
    public Address setZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    /**
     * @return Возвращает текстовое представление адреса.
     */
    @Override
    public String toString() {
        return "Address(street=" + street + ", zipCode=" + zipCode + ")";
    }

    /**
     * Преобразует объект в строку CSV.
     * @return строка CSV
     */
    public String toCsv() {
        return street + ";" + (zipCode == null ? "<NULL>" : zipCode);
    }

    /**
     * Создает объект из строки CSV.
     * @param csv строка CSV
     * @return объект Address
     */
    public static Address fromCsv(String csv) {
        String[] parts = csv.split(";", -1);
        if (parts.length != 2) return null;

        String street = parts[0].equals("<NULL>") ? null : parts[0];
        String zipCode = parts[1].equals("<NULL>") ? null : parts[1];

        return new Address()
            .setStreet(street)
            .setZipCode(zipCode);
    }
}
