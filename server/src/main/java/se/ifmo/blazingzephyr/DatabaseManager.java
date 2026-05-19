package se.ifmo.blazingzephyr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Stack;

import org.postgresql.util.PGobject;

import se.ifmo.blazingzephyr.model.Address;
import se.ifmo.blazingzephyr.model.Coordinates;
import se.ifmo.blazingzephyr.model.Organization;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.model.OrganizationType;

public class DatabaseManager {
    
    private final Connection connection;
    
    public DatabaseManager(String url, Properties properties) throws SQLException {
        this.connection = DriverManager.getConnection(url, properties);
        createTableOnStartup();
    }

    private void createTableOnStartup() throws SQLException {
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
            )
            """;

        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    /**
     * Получает список организаций с БД.
     * @return Организации.
     */
    public Stack<Organization> selectAll() throws SQLException {
        Stack<Organization> collection = new Stack<>();
        String sql = "SELECT * FROM organizations";

        try (Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                collection.push(mapRow(rs));
            }
        }
        return collection;
    }

    /**
     * Получает список организаций в обратном порядке.
     * @return Организации.
     */
    public Stack<Organization> selectAllReverse() throws SQLException {
        Stack<Organization> collection = new Stack<>();
        String sql = "SELECT * FROM organizations ORDER BY id DESC";

        try (Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                collection.push(mapRow(rs));
            }
        }
        return collection;
    }

    /**
     * Получает список организаций, тип которых больше указанного, с сервера.
     * @param type Тип организации, с которым будут сравнивать.
     * @return Организации.
     */
    public Stack<Organization> selectAllGreaterThanType(OrganizationType type) throws SQLException {
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

        return result;
    }

    /**
     * Пытается получить время создания таблицы.
     * Может быть неубедительным.
     * 
     * @return дата и время создания таблицы организаций.
     */
    public Optional<Timestamp> creationDate() throws SQLException {
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
                    return Optional.of(rs.getTimestamp("creation"));
                }

                return Optional.empty();
        }
    }

    /**
     * Возвращает колонки таблицы.
     * @return название колонок + их тип.
     */
    public Stack<String> getColumns() throws SQLException {
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

                return rows;
        }
    }

    /**
     * Возвращает число элементов в таблице организаций.
     * @return число элементов.
     */
    public Optional<Long> count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM organizations";
        try (Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    return Optional.of(rs.getLong("count"));
                }

                return Optional.empty();
        }
    }

    public Optional<Organization> getMinByName() throws SQLException {
        String sql = "SELECT * FROM organizations ORDER BY name ASC LIMIT 1";
        try (Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    return Optional.of(mapRow(rs));
                }

                return Optional.empty();
        }
    }

    /**
     * Добавляет новую организацию в таблицу.
     * @param data Данные организации, которую необходимо добавить.
     * @return Добавленный элемент.
     */
    public Organization insert(OrganizationData data) throws SQLException {
        String sql = """
            INSERT INTO organizations
              (name, coord_x, coord_y, annual_turnover,
                full_name, organization_type, street, zip_code)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING (id, name, coord_x, coord_y, creation_date, annual_turnover,
                full_name, organization_type, street, zip_code)
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

            ResultSet rs = ps.executeQuery();
            rs.next();
            return mapRow(rs);
        }
    }

    /**
     * Обновляет существующий объект БД по ID.
     * @param id ID объекта, который требуется обновить.
     * @param data Данные объекта, который требуется обновить.
     */
    public boolean update(long id, OrganizationData data) throws SQLException {
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
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Удаляет запись в таблицу по её ID.
     * @param id ID организации, которую требуется удалить из базы данных.
     * @return true, если операция успешна, в противном случае false.
     */
    public boolean deleteById(long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM organizations WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Очищает таблицу.
     */
    public void deleteAll() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM organizations");
        }
    }

    /**
     * Очищает таблицу.
     * @param rs Результаты запроса к базе данных.
     * @return Объект организации.
     */
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

        return org;
    }

    /**
     * Закрывает подключение к базе данных.
     */
    public void close() throws SQLException {
        connection.close();
    }
}
