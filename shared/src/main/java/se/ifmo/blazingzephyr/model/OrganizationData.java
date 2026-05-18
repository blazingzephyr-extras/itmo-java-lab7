package se.ifmo.blazingzephyr.model;

import java.io.Serializable;
import java.util.Scanner;

/**
 * Класс для интерактивного чтения полей Organization.
 * @author blazingzephyr
 * @version 1.0
 */
public class OrganizationData implements Serializable {

    private String name;
    private Coordinates coordinates;
    private double annualTurnover;
    private String fullName;
    private OrganizationType organizationType;
    private Address officialAddress;

    /**
     * Возвращает наименование организации.
     * @return Строка имени.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Возвращает координаты организации.
     * @return координаты.
     */
    public Coordinates getCoordinates() {
        return this.coordinates;
    }
    
    /**
     * Возвращает ежегодную выручку организации.
     * @return ежегодная выручка организации.
     */
    public Double getAnnualTurnover() {
        return this.annualTurnover;
    }

    /**
     * Возвращает полное наименование организации.
     * @return Строка имени.
     */
    public String getFullName() {
        return this.fullName;
    }
    
    /**
     * Возвращает тип данной организации.
     * @return перечисление, отождествлённое с типом.
     */
    public OrganizationType getOrganizationType() {
        return this.organizationType;
    }
    
    /**
     * Возвращает координаты организации.
     * @return координаты.
     */
    public Address getOfficialAddress() {
        return this.officialAddress;
    }
    

    /**
     * Запрашивает обновлённые значения полей организации и присваивает их полям.
     */
    public OrganizationData query(Scanner scanner)
    {
        queryName(scanner);
        askCoordinates(scanner);
        askAnnualTurnover(scanner);
        askFullName(scanner);
        askType(scanner);
        askAddress(scanner);
        return this;
    }

    /**
     * Запрашивает у пользователя имя и производит его валидацию.
     */
    private void queryName(Scanner scanner) {
        while (true) {
            System.out.println("Название организации: String, не может быть пустым.");
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                this.name = input;
                return;
            }
            System.out.println("Некорректные данные: названием не может быть пустым.");
        }
    }

    /**
     * Запрашивает у пользователя координаты и производит их валидацию.
     */
    private void askCoordinates(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Координаты организации: [x: float; y: float].");
                String input = scanner.nextLine().trim();
                String[] coord = input.split(" ");
                
                if (input.isBlank()) throw new IllegalArgumentException("Введена пустая строка в поле 'координаты'.");
                if (coord.length < 2) throw new IllegalArgumentException("Недостаточно аргументов в поле 'координаты'.");

                this.coordinates = new Coordinates()
                        .setX(Double.valueOf(coord[0]))
                        .setY(Float.parseFloat(coord[1]));
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("Некорректные данные: " + ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Запрашивает у пользователя данные о годовой выручке компании и возвращает их после валидации
     */
    private void askAnnualTurnover(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Ежегодная выручка: Double, не может быть null, должно быть больше нуля.");
                String input = scanner.nextLine().trim();
                Double turnover = Double.valueOf(input);
                if (turnover <= 0) throw new IllegalArgumentException("Должно быть больше нуля.");
                this.annualTurnover = turnover;
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("Некорректные данные: " + ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Запрашивает у пользователя полное имя организации и возвращает данные после валидации.
     * @return полное имя.
     */
    private void askFullName(Scanner scanner) {
        while (true) {
            System.out.println("Полное название: String, не может быть пустым.");
            String input = scanner.nextLine().trim();
            
            if (!input.isEmpty()) {
                this.fullName = input;
                return;
            }
            System.out.println("Некорректные данные: названием не может быть пустым.");
        }
    }

    /**
     * Запрашивает у пользователя полный тип организации и возвращает его после валидации.
     * @return тип организации.
     */
    private void askType(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Тип организации: " + OrganizationType.getTypes() + ".");
                String input = scanner.nextLine().trim().toUpperCase();
                this.organizationType = OrganizationType.valueOf(input);
                return;
            } catch (Exception ex) {
                System.out.println("Некорректные данные: Такого типа не существует.");
            }
        }
    }

    /**
     * Запрашивает улицу и почтовый код организации.
     * @return полный адрес организации.
     */
    private void askAddress(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Адрес: [street: String, не null; zipCode: String, не null].");
                String input = scanner.nextLine().trim();
                String[] addr = input.split(" ");

                if (input.isBlank()) throw new IllegalArgumentException("Введена пустая строка в поле 'адрес'.");
                if (addr.length < 2) throw new IllegalArgumentException("Недостаточно аргументов в поле 'адрес'.");

                this.officialAddress = new Address()
                        .setStreet(addr[0])
                        .setZipCode(addr[1]);
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("Некорректные данные: " + ex.getLocalizedMessage());
            }
        }
    }
}
