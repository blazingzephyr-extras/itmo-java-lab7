package se.ifmo.blazingzephyr.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс организации, хранящейся в коллекции.
 * Программа управляет коллекцией его экземпляров.
 * Реализует интерфейс Comparable для предоставления сортировки по умолчанию (по id).
 * @author blazingzephyr
 * @version 1.0
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
     * Конструктор для создания новой организации (с id и датой).
     */
    public Organization() {
        this.creationDate = new Date();
        this.id = this.creationDate.getTime();

        this.name = "<MISSING>";
        this.coordinates = new Coordinates();
        this.annualTurnover = 1.0;
        this.fullName = "<MISSING>";
        this.type = OrganizationType.GOVERNMENT;
        this.officialAddress = new Address();
    }

    /**
     * Конструктор для создания новой организации из объекта OrganizationData.
     * @param data данные, откуда будет взята.
     */
    public Organization(OrganizationData data) {
        this.creationDate = new Date();
        this.id = this.creationDate.getTime();

        this.name = data.getName();
        this.coordinates = data.getCoordinates();
        this.annualTurnover = data.getAnnualTurnover();
        this.fullName = data.getFullName();
        this.type = data.getOrganizationType();
        this.officialAddress = data.getOfficialAddress();
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
     * Преобразует объект в строку CSV.
     * @return строка CSV.
     */
    public String toCsv() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return id + ";" + name + ";" + coordinates.toCsv() + ";" + sdf.format(creationDate) + ";" +
            annualTurnover + ";" + fullName + ";" + type + ";" + officialAddress.toCsv();
    }
    
    /**
     * Создает объект из строки CSV.
     * @param csv строка CSV
     * @return объект Organization или null при ошибке
     */
    public static Organization fromCsv(String csv) {
        String[] parts = csv.split(";", -1);
        if (parts.length != 10) return null;

        long id = Long.parseLong(parts[0]);
        
        String name = parts[1];
        Coordinates coordinates = Coordinates.fromCsv(parts[2] + ";" + parts[3]);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date creationDate;
        try {
            creationDate = sdf.parse(parts[4]);
        }
        catch (ParseException exception) {
            throw new IllegalArgumentException("Некорректный формат даты.");
        }

        double annualTurnover = Double.parseDouble(parts[5]);
        String fullName = parts[6];
        OrganizationType type = OrganizationType.valueOf(parts[7]);
        Address address = Address.fromCsv(parts[8] + ";" + parts[9]);

        Organization organization = new Organization()
            .setName(name)
            .setCoordinates(coordinates)
            .setAnnualTurnover(annualTurnover)
            .setFullName(fullName)
            .setOrganizationType(type)
            .setOfficialAddress(address);

        organization.id = id;
        organization.creationDate = creationDate;
        return organization;
    }

    /**
     * Сортировка по name.
     */
    @Override
    public int compareTo(Organization o) {
        return this.name.compareTo(o.getName());
    }
}
