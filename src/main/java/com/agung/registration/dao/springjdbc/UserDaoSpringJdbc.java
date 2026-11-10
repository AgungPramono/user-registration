package com.agung.registration.dao.springjdbc;

import com.agung.registration.dao.UserDao;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
import com.agung.registration.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoSpringJdbc implements UserDao {

    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert simpleInsert;

    private static final String BASE_SELECT = """
        SELECT u.id, u.username, u.email, u.password_hash, u.full_name, u.city_id, u.status, u.created_at, u.updated_at,
               c.name AS city_name, c.created_at AS city_created_at,
               p.id AS province_id, p.code AS province_code, p.name AS province_name, p.created_at AS province_created_at
        FROM users u
        JOIN cities c ON u.city_id = c.id
        JOIN provinces p ON c.province_id = p.id
        """;

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setCityId(rs.getLong("city_id"));
        u.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            u.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            u.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        City c = new City();
        c.setId(rs.getLong("city_id"));
        c.setProvinceId(rs.getLong("province_id"));
        c.setName(rs.getString("city_name"));
        Timestamp cityCreatedAt = rs.getTimestamp("city_created_at");
        if (cityCreatedAt != null) {
            c.setCreatedAt(cityCreatedAt.toLocalDateTime());
        }

        Province p = new Province();
        p.setId(rs.getLong("province_id"));
        p.setCode(rs.getString("province_code"));
        p.setName(rs.getString("province_name"));
        Timestamp provCreatedAt = rs.getTimestamp("province_created_at");
        if (provCreatedAt != null) {
            p.setCreatedAt(provCreatedAt.toLocalDateTime());
        }
        c.setProvince(p);
        u.setCity(c);

        return u;
    };

    public UserDaoSpringJdbc(DataSource dataSource) {
        this.jdbcClient = JdbcClient.create(dataSource);
        this.simpleInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbcClient.sql(BASE_SELECT + " WHERE u.id = :id")
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbcClient.sql(BASE_SELECT + " WHERE u.username = :username")
                .param("username", username)
                .query(ROW_MAPPER)
                .optional();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbcClient.sql(BASE_SELECT + " WHERE u.email = :email")
                .param("email", email)
                .query(ROW_MAPPER)
                .optional();
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcClient.sql("SELECT COUNT(1) FROM users WHERE username = :username")
                .param("username", username)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcClient.sql("SELECT COUNT(1) FROM users WHERE email = :email")
                .param("email", email)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            Number newId = simpleInsert.executeAndReturnKey(new MapSqlParameterSource()
                    .addValue("username", user.getUsername())
                    .addValue("email", user.getEmail())
                    .addValue("password_hash", user.getPasswordHash())
                    .addValue("full_name", user.getFullName())
                    .addValue("city_id", user.getCityId())
                    .addValue("status", user.getStatus() != null ? user.getStatus() : "ACTIVE"));
            user.setId(newId.longValue());
            return user;
        } else {
            jdbcClient.sql("""
                    UPDATE users
                    SET username = :username, email = :email, password_hash = :passwordHash,
                        full_name = :fullName, city_id = :cityId, status = :status
                    WHERE id = :id
                    """)
                    .param("username", user.getUsername())
                    .param("email", user.getEmail())
                    .param("passwordHash", user.getPasswordHash())
                    .param("fullName", user.getFullName())
                    .param("cityId", user.getCityId())
                    .param("status", user.getStatus())
                    .param("id", user.getId())
                    .update();
            return user;
        }
    }

    @Override
    public List<User> findAll() {
        return jdbcClient.sql(BASE_SELECT + " ORDER BY u.created_at DESC")
                .query(ROW_MAPPER)
                .list();
    }
}
