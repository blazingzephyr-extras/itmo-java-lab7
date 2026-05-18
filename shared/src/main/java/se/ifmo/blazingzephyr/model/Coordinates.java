package se.ifmo.blazingzephyr.model;

import java.io.Serializable;

/**
 * Координаты организации.
 * @author blazingzephyr
 * @version 1.0
 */
public final class Coordinates implements Serializable {
    private Double x;   // Максимальное значение поля: 213, Поле не может быть null
    private float y;

    /**
     * Пустой конструктор координат.
     * Устанавливает координаты в точку (0, 0).
     */
    public Coordinates() {
        this.x = 0.0;
        this.y = 0;
    }
    
    /**
     * @return Значение x.
     */
    public Double getX() {
        return x;
    }
    
    /**
     * Устанавливает новое значение координате x.
     * @param x координата X (не может быть null, максимальное значение 213).
     * @throws IllegalArgumentException
     */
    public Coordinates setX(Double x) {
        if (x == null) {
            throw new IllegalArgumentException("Поле 'x' не может быть null.");
        }
        if (x > 213) {
            throw new IllegalArgumentException("Максимальное значение 'x' - 213.");
        }

        this.x = x;
        return this;
    }
    
    /**
     * @return Значение y.
     */
    public float getY() {
        return y;
    }
    
    /**
     * Устанавливает новое значение координате y.
     * @param y Координата Y.
     */
    public Coordinates setY(float y) {
        this.y = y;
        return this;
    }
    
    /**
     * Возвращает графическое представление данной строки.
     * @return координатная строка.
     */
    @Override
    public String toString() {
        return "Coordinates(x=" + x + ", y=" + y + ")";
    }
    
    /**
     * Преобразует объект в строку CSV.
     * @return строка CSV.
     */
    public String toCsv() {
        return x + ";" + y;
    }
    
    /**
     * Создает объект из строки CSV.
     * @param csv строка CSV.
     * @return объект Coordinates.
     */
    public static Coordinates fromCsv(String csv) {
        String[] parts = csv.split(";");
        if (parts.length != 2) return null;

        String xString = parts[0].equals("<NULL>") ? null : parts[0];
        double x = Double.parseDouble(xString);
        float y = Float.parseFloat(parts[1]);

        return new Coordinates()
            .setX(x)
            .setY(y);
    }
}
