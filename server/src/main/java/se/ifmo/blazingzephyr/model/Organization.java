package se.ifmo.blazingzephyr.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Класс организации, хранящейся в коллекции.
 * Программа управляет коллекцией его экземпляров.
 * Реализует интерфейс Comparable для предоставления сортировки по умолчанию (по id).
 * 
 * По причине ненадобности со стороны клиента и для инкапсуляции данных
 * объект БД перемещён на сторону сервера.
 * 
 * Парсировка из-в CSV была удалена из-за ненадобности.
 * 
 * @author blazingzephyr
 * @version 2.0
 */
public class Organization implements Comparable<Organization>, Serializable {
    
    private Long id;                        // Поле не может быть null, Значение поля должно быть больше 0, уникальное, генерируется автоматически
    private String name;                    // Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates;        // Поле не может быть null
    private java.util.Date creationDate;    // Поле не может быть null, генерируется автоматически
    private Double annualTurnover;          // Поле не может быть null, Значение поля должно быть больше 0
    private String fullName;                // Строка не может быть пустой, Поле не может быть null
    private OrganizationType type;          // Поле не может быть null
    private Address officialAddress;        // Поле не может быть null

    /**
     * Конструктор для создания организации.
     * @param data данные, откуда будет взята.
     */
    public Organization(long id, Date creationDate) {
        this.creationDate = creationDate;
        this.id = id;

        this.name = "<MISSING>";
        this.coordinates = new Coordinates();
        this.annualTurnover = 1.0;
        this.fullName = "<MISSING>";
        this.type = OrganizationType.GOVERNMENT;
        this.officialAddress = new Address();
    }

    /**
     * Возвращает автоматически сгенерированный уникальный ID этой генерации.
     * @return Long, представляющий ID.
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Возвращает наименование организации.
     * @return Строка имени.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Присваивает новое имя организации.
     * @param name новое имя (Поле не может быть null, Строка не может быть пустой).
     * @return этот объект Organization.
     */
    public Organization setName(String name) {
        if (name == null)
        {
            throw new IllegalArgumentException("Поле 'name' не может быть null.");
        }
        else if (name.isEmpty())
        {
            throw new IllegalArgumentException("Строка 'name' не может быть пустой.");
        }

        this.name = name;
        return this;
    }
    
    /**
     * Возвращает координаты организации.
     * @return координаты.
     */
    public Coordinates getCoordinates() {
        return this.coordinates;
    }
    
    /**
     * Назначает организации новые координаты.
     * @param coordinates новые координаты организации
     * @return этот объект Organization.
     */
    public Organization setCoordinates(Coordinates coordinates) {
        if (coordinates == null)
        {
            throw new IllegalArgumentException("Поле 'coordinates' не может быть null.");
        }

        this.coordinates = coordinates;
        return this;
    }

    /**
     * Возвращает дату создания организации.
     * @return Автоматически сгенерированный объект даты.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Возвращает ежегодную выручку организации.
     * @return ежегодная выручка организации.
     */
    public Double getAnnualTurnover() {
        return this.annualTurnover;
    }

    /**
     * Устанавливает ежегодную выручку организации.
     * @param name значение выручки (Поле не может быть null, Значение поля должно быть больше 0).
     * @return этот объект Organization.
     */
    public Organization setAnnualTurnover(Double annualTurnover) {
        if (annualTurnover == null)
        {
            throw new IllegalArgumentException("Поле 'annualTurnover' не может быть null.");
        }
        if (annualTurnover <= 0)
        {
            throw new IllegalArgumentException("Значение 'annualTurnover' должно быть больше 0.");
        }

        this.annualTurnover = annualTurnover;
        return this;
    }
    
    /**
     * Возвращает полное наименование организации.
     * @return Строка имени.
     */
    public String getFullName() {
        return this.fullName;
    }
    
    /**
     * Присваивает новое полное имя организации.
     * @param name новое полное имя (Поле не может быть null, Строка не может быть пустой).
     * @return этот объект Organization.
     */
    public Organization setFullName(String fullName) {
        if (fullName == null)
        {
            throw new IllegalArgumentException("Поле 'fullName' не может быть null.");
        }
        else if (fullName.isEmpty())
        {
            throw new IllegalArgumentException("Строка 'fullName' не может быть пустой.");
        }

        this.fullName = fullName;
        return this;
    }
    
    /**
     * Возвращает тип данной организации.
     * @return перечисление, отождествлённое с типом.
     */
    public OrganizationType getOrganizationType() {
        return this.type;
    }
    
    /**
     * Назначает тип данной организации.
     * @param type новый тип организации
     * @return этот объект Organization.
     */
    public Organization setOrganizationType(OrganizationType type) {
        if (type == null)
        {
            throw new IllegalArgumentException("Поле 'type' не может быть null.");
        }

        this.type = type;
        return this;
    }

    /**
     * Возвращает координаты организации.
     * @return координаты.
     */
    public Address getOfficialAddress() {
        return this.officialAddress;
    }
    
    /**
     * Назначает организации новый официальный адрес.
     * @param coordinates новый официальный адрес организации.
     * @return этот объект Organization.
     */
    public Organization setOfficialAddress(Address address) {
        if (address == null)
        {
            throw new IllegalArgumentException("Поле 'address' не может быть null.");
        }

        this.officialAddress = address;
        return this;
    }

    /**
     * Возвращает графическое представление данной строки.
     * @return координатная строка.
     */
    @Override
    public String toString() {
        return "Organization(id=" + id + ", name=" + name + ", coordinates=" + coordinates + ", creationDate=" +
            creationDate + ", annualTurnover=" + annualTurnover + ", fullName=" + fullName + ", type=" + type +
                            ", officialAddress=" + officialAddress + ")";
    }

    /**
     * Сортировка по name.
     */
    @Override
    public int compareTo(Organization o) {
        return this.name.compareTo(o.getName());
    }
}
