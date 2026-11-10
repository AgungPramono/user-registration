package com.agung.registration.dao.jdbc;

import com.agung.registration.dao.ProvinceDao;
import com.agung.registration.exception.DataAccessException;
import com.agung.registration.model.Province;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProvinceDaoJdbc implements ProvinceDao {

    private final DataSource dataSource;

    public ProvinceDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Province> findById(Long id) {
        String sql = "SELECT id, code, name, created_at FROM provinces WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProvince(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query province by id: " + id, e);
        }
    }

    @Override
    public Optional<Province> findByCode(String code) {
        String sql = "SELECT id, code, name, created_at FROM provinces WHERE code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProvince(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query province by code: " + code, e);
        }
    }

    @Override
    public List<Province> findAll() {
        String sql = "SELECT id, code, name, created_at FROM provinces ORDER BY name ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Province> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToProvince(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all provinces", e);
        }
    }

    @Override
    public Province save(Province province) {
        if (province.getId() == null) {
            String sql = "INSERT INTO provinces (code, name) VALUES (?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, province.getCode());
                ps.setString(2, province.getName());
                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        province.setId(generatedKeys.getLong(1));
                    }
                }
                return province;
            } catch (SQLException e) {
                throw new DataAccessException("Failed to insert province: " + province.getName(), e);
            }
        } else {
            String sql = "UPDATE provinces SET code = ?, name = ? WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, province.getCode());
                ps.setString(2, province.getName());
                ps.setLong(3, province.getId());
                ps.executeUpdate();
                return province;
            } catch (SQLException e) {
                throw new DataAccessException("Failed to update province with id: " + province.getId(), e);
            }
        }
    }

    private Province mapResultSetToProvince(ResultSet rs) throws SQLException {
        Province p = new Province();
        p.setId(rs.getLong("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
