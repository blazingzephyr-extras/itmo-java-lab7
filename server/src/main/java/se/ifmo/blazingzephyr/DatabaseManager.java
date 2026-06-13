package se.ifmo.blazingzephyr;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.Properties;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PGobject;

import se.ifmo.blazingzephyr.model.Address;
import se.ifmo.blazingzephyr.model.Coordinates;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.model.OrganizationType;

public class DatabaseManager {

    private static final Logger log = LogManager.getLogger(DatabaseManager.class);

    private final Connection connection;

    public DatabaseManager(String url, Properties properties) throws SQLException {
        log.debug("Попытка установить соединение с БД: {}", url);
        this.connection = DriverManager.getConnection(url, properties);
        log.info("Соединение с БД установлено.");
        createTableOnStartup();
    }

    private void createTableOnStartup() throws SQLException {
        log.debug("Проверка и создание схемы БД (таблицы organizations, users)...");
        String sql = """
            DO $$ BEGIN
                CREATE TYPE ORGANIZATIONTYPE AS ENUM (
                    'GOVERNMENT',
                    'TRUST',
                    'PRIVATE_LIMITED_COMPANY',
                    'OPEN_JOINT_STOCK_COMPANY'
                );
            EXCEPTION
                WHEN duplicate_object THEN null;
            END $$;

            CREATE TABLE IF NOT EXISTS organizations (
                id                  BIGSERIAL                   PRIMARY KEY,
                name                VARCHAR(255)                NOT NULL,
                coord_x             DOUBLE PRECISION            NOT NULL,
                coord_y             REAL                        NOT NULL,
                creation_date       TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
                annual_turnover     BIGINT,
                full_name           VARCHAR(255),
                organization_type   ORGANIZATIONTYPE            NOT NULL,
                street              VARCHAR(255),
                zip_code            VARCHAR(50)
            );

            CREATE TABLE IF NOT EXISTS users (
                id       BIGSERIAL      PRIMARY KEY,
                login    VARCHAR(255)   NOT NULL UNIQUE,
                password VARCHAR(255)   NOT NULL  -- MD2 хеш
            );

            ALTER TABLE organizations
                ADD COLUMN IF NOT EXISTS owner VARCHAR(255) REFERENCES users(login);
            """;

        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
        }
        log.info("Схема БД успешно проверена/создана.");
    }

    /**
     * Регистрация нового пользователя.
     */
    public boolean registerUser(String login, String password) throws SQLException {
        log.debug("Попытка регистрации пользователя: '{}'", login);
        String hash = hashMD2(password);
        String sql = "INSERT INTO users (login, password) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, hash);
            boolean registered = ps.executeUpdate() > 0;
            if (registered) {
                log.info("Пользователь '{}' успешно зарегистрирован.", login);
            } else {
                log.warn("Попытка регистрации с уже занятым логином: '{}'", login);
            }
            return registered;
        }
    }

    /**
     * Проверка логина/пароля.
     */
    public boolean authenticate(String login, String password) throws SQLException {
        log.debug("Попытка аутентификации пользователя: '{}'", login);
        String hash = hashMD2(password);
        String sql = "SELECT 1 FROM users WHERE login=? AND password=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, hash);
            ResultSet rs = ps.executeQuery();
            boolean ok = rs.next();
            if (ok) {
                log.info("Пользователь '{}' успешно аутентифицирован.", login);
            } else {
                log.warn("Неудачная попытка аутентификации для логина: '{}'", login);
            }
            return ok;
        }
    }

    // MD2 хеширование
    private String hashMD2(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Алгоритм MD2 недоступен.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Получает организацию по ID с БД.
     */
    public Optional<Organization> selectById(long id) throws SQLException {
        log.debug("Запрос организации по ID={}.", id);
        String sql = "SELECT * FROM organizations WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                log.debug("Организация с ID={} найдена.", id);
                return Optional.of(mapRow(rs));
            }
        }
        log.debug("Организация с ID={} не найдена.", id);
        return Optional.empty();
    }

    /**
     * Получает список организаций с БД.
     */
    public Stack<Organization> selectAll() throws SQLException {
        log.debug("Запрос всех организаций из БД.");
        Stack<Organization> collection = new Stack<>();
        String sql = "SELECT * FROM organizations";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                collection.push(mapRow(rs));
            }
        }
        log.info("Получено {} организаций из БД.", collection.size());
        return collection;
    }

    /**
     * Получает список организаций в обратном порядке.
     */
    public Stack<Organization> selectAllReverse() throws SQLException {
        log.debug("Запрос всех организаций в обратном порядке.");
        Stack<Organization> collection = new Stack<>();
        String sql = "SELECT * FROM organizations ORDER BY id DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                collection.push(mapRow(rs));
            }
        }
        log.debug("Получено {} организаций (обратный порядок).", collection.size());
        return collection;
    }

    /**
     * Получает список организаций, тип которых больше указанного.
     */
    public Stack<Organization> selectAllGreaterThanType(OrganizationType type) throws SQLException {
        log.debug("Запрос организаций с типом > {}.", type);
        String sql = """
            SELECT * FROM organizations
            WHERE
                CASE organization_type
                    WHEN 'GOVERNMENT'               THEN 0
                    WHEN 'TRUST'                    THEN 1
                    WHEN 'PRIVATE_LIMITED_COMPANY'  THEN 2
                    WHEN 'OPEN_JOINT_STOCK_COMPANY' THEN 3
                END > ?
            ORDER BY street ASC NULLS LAST
            """;

        Stack<Organization> result = new Stack<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, type.ordinal());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        log.debug("Найдено {} организаций с типом > {}.", result.size(), type);
        return result;
    }

    /**
     * Пытается получить время создания таблицы.
     */
    public Optional<Timestamp> creationDate() throws SQLException {
        log.debug("Запрос даты создания таблицы organizations.");
        String sql = """
            SELECT creation
            FROM pg_stat_file(
                './base/'
                ||
                (
                    SELECT
                    MAX(pg_ls_dir::bigint)::text
                    FROM pg_ls_dir('./base')
                    WHERE pg_ls_dir <> 'pgsql_tmp'
                    AND pg_ls_dir::bigint <= (SELECT relfilenode FROM pg_class WHERE relname ILIKE 'organizations')
                )
                || '/' || (SELECT relfilenode::text FROM pg_class WHERE relname ILIKE 'organizations')
            )
            """;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("creation");
                log.debug("Дата создания таблицы: {}.", ts);
                return Optional.of(ts);
            }
            log.warn("Не удалось определить дату создания таблицы organizations.");
            return Optional.empty();
        }
    }

    /**
     * Возвращает колонки таблицы.
     */
    public Stack<String> getColumns() throws SQLException {
        log.debug("Запрос метаданных колонок таблицы organizations.");
        String sql = """
            SELECT COLUMN_NAME, DATA_TYPE
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_NAME= 'organizations';
            """;

        Stack<String> rows = new Stack<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(rs.getString("COLUMN_NAME") + ": " + rs.getString("DATA_TYPE"));
            }
            log.debug("Получено {} колонок.", rows.size());
            return rows;
        }
    }

    /**
     * Возвращает число элементов в таблице организаций.
     */
    public Optional<Long> count() throws SQLException {
        log.debug("Запрос количества организаций в БД.");
        String sql = "SELECT COUNT(*) FROM organizations";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                long n = rs.getLong("count");
                log.debug("Количество организаций: {}.", n);
                return Optional.of(n);
            }
            return Optional.empty();
        }
    }

    public Optional<Organization> getMinByName() throws SQLException {
        log.debug("Запрос организации с минимальным именем.");
        String sql = "SELECT * FROM organizations ORDER BY name ASC LIMIT 1";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Organization org = mapRow(rs);
                log.debug("Организация с минимальным именем: '{}' (ID={}).", org.getName(), org.getId());
                return Optional.of(org);
            }
            log.debug("Таблица организаций пуста — минимальный элемент не найден.");
            return Optional.empty();
        }
    }

    /**
     * Добавляет новую организацию в таблицу.
     */
    public Organization insert(OrganizationData data, String owner) throws SQLException {
        log.debug("Вставка новой организации '{}' для пользователя '{}'.", data.getName(), owner);
        String sql = """
            INSERT INTO organizations
              (name, coord_x, coord_y, annual_turnover,
                full_name, organization_type, street, zip_code, owner)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING *
            """;

        PGobject pgType = new PGobject();
        pgType.setType("ORGANIZATIONTYPE");
        pgType.setValue(data.getOrganizationType().name());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getName());
            ps.setDouble(2, data.getCoordinates().getX());
            ps.setFloat(3, data.getCoordinates().getY());
            ps.setDouble(4, data.getAnnualTurnover());
            ps.setString(5, data.getFullName());
            ps.setObject(6, pgType);
            ps.setString(7, data.getOfficialAddress().getStreet());
            ps.setString(8, data.getOfficialAddress().getZipCode());
            ps.setString(9, owner);

            ResultSet rs = ps.executeQuery();
            rs.next();
            Organization org = mapRow(rs);
            log.info("Организация '{}' успешно добавлена (ID={}, owner='{}').", org.getName(), org.getId(), owner);
            return org;
        }
    }

    /**
     * Обновляет существующий объект БД по ID.
     */
    public boolean update(long id, OrganizationData data) throws SQLException {
        log.debug("Обновление организации ID={}.", id);
        String sql = """
            UPDATE organizations SET
              name=?, coord_x=?, coord_y=?,
              annual_turnover=?, full_name=?,
              organization_type=?, street=?, zip_code=?
            WHERE id=?
            """;

        PGobject pgType = new PGobject();
        pgType.setType("ORGANIZATIONTYPE");
        pgType.setValue(data.getOrganizationType().name());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getName());
            ps.setDouble(2, data.getCoordinates().getX());
            ps.setFloat(3, data.getCoordinates().getY());
            ps.setDouble(4, data.getAnnualTurnover());
            ps.setString(5, data.getFullName());
            ps.setObject(6, pgType);
            ps.setString(7, data.getOfficialAddress().getStreet());
            ps.setString(8, data.getOfficialAddress().getZipCode());
            ps.setLong(9, id);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                log.info("Организация ID={} успешно обновлена.", id);
            } else {
                log.warn("Организация ID={} не найдена для обновления.", id);
            }
            return updated;
        }
    }

    /**
     * Удаляет запись в таблице по её ID.
     */
    public boolean deleteById(long id) throws SQLException {
        log.debug("Удаление организации ID={}.", id);
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM organizations WHERE id=?")) {
            ps.setLong(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                log.info("Организация ID={} успешно удалена.", id);
            } else {
                log.warn("Организация ID={} не найдена для удаления.", id);
            }
            return deleted;
        }
    }

    /**
     * Удаляет все организации указанного владельца.
     */
    public void deleteAll(String login) throws SQLException {
        log.debug("Удаление всех организаций пользователя '{}'.", login);
        try (Statement st = connection.createStatement()) {
            int affected = st.executeUpdate("DELETE FROM organizations WHERE owner='" + login + "'");
            log.info("Удалено {} организаций пользователя '{}'.", affected, login);
        }
    }

    private Organization mapRow(ResultSet rs) throws SQLException {
        Organization org = new Organization(
            rs.getLong("id"),
            rs.getTimestamp("creation_date")
        );

        org.setName(rs.getString("name"));
        org.setCoordinates(
            new Coordinates()
                .setX(rs.getDouble("coord_x"))
                .setY(rs.getFloat("coord_y"))
        );

        org.setAnnualTurnover(rs.getDouble("annual_turnover"));
        org.setFullName(rs.getString("full_name"));
        org.setOrganizationType(OrganizationType.valueOf(rs.getString("organization_type")));

        org.setOfficialAddress(
            new Address()
                .setStreet(rs.getString("street"))
                .setZipCode(rs.getString("zip_code"))
        );

        org.setOwner(rs.getString("owner"));
        return org;
    }

    /**
     * Закрывает подключение к базе данных.
     */
    public void close() throws SQLException {
        log.info("Закрытие соединения с БД.");
        connection.close();
        log.debug("Соединение с БД закрыто.");
    }
}
