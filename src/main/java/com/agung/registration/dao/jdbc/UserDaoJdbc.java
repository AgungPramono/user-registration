package com.agung.registration.dao.jdbc;

import com.agung.registration.dao.UserDao;
import com.agung.registration.exception.DataAccessException;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
import com.agung.registration.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoJdbc implements UserDao {

    private final DataSource dataSource;

    private static final String BASE_SELECT = """
        SELECT u.id, u.username, u.email, u.password_hash, u.full_name, u.city_id, u.status, u.created_at, u.updated_at,
               c.name AS city_name, c.created_at AS city_created_at,
               p.id AS province_id, p.code AS province_code, p.name AS province_name, p.created_at AS province_created_at
        FROM users u
        JOIN cities c ON u.city_id = c.id
        JOIN provinces p ON c.province_id = p.id
        """;

    public UserDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = BASE_SELECT + " WHERE u.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query user by id: " + id, e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = BASE_SELECT + " WHERE u.username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query user by username: " + username, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = BASE_SELECT + " WHERE u.email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query user by email: " + email, e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check username existence: " + username, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check email existence: " + email, e);
        }
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            String sql = """
                INSERT INTO users (username, email, password_hash, full_name, city_id, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getFullName());
                ps.setLong(5, user.getCityId());
                ps.setString(6, user.getStatus() != null ? user.getStatus() : "ACTIVE");
                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }
                return user;
            } catch (SQLException e) {
                throw new DataAccessException("Failed to insert user: " + user.getUsername(), e);
            }
        } else {
            String sql = """
                UPDATE users
                SET username = ?, email = ?, password_hash = ?, full_name = ?, city_id = ?, status = ?
                WHERE id = ?
                """;
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getFullName());
                ps.setLong(5, user.getCityId());
                ps.setString(6, user.getStatus());
                ps.setLong(7, user.getId());
                ps.executeUpdate();
                return user;
            } catch (SQLException e) {
                throw new DataAccessException("Failed to update user id: " + user.getId(), e);
            }
        }
    }

    @Override
    public List<User> findAll() {
        String sql = BASE_SELECT + " ORDER BY u.created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all users", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
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
    }
}
