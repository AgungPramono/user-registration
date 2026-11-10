package com.agung.registration.dao.jdbc;

import com.agung.registration.dao.CityDao;
import com.agung.registration.exception.DataAccessException;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CityDaoJdbc implements CityDao {

    private final DataSource dataSource;

    public CityDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<City> findById(Long id) {
        String sql = """
            SELECT c.id, c.province_id, c.name, c.created_at,
                   p.code AS province_code, p.name AS province_name, p.created_at AS province_created_at
            FROM cities c
            JOIN provinces p ON c.province_id = p.id
            WHERE c.id = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCity(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query city by id: " + id, e);
        }
    }

    @Override
    public List<City> findByProvinceId(Long provinceId) {
        String sql = """
            SELECT c.id, c.province_id, c.name, c.created_at,
                   p.code AS province_code, p.name AS province_name, p.created_at AS province_created_at
            FROM cities c
            JOIN provinces p ON c.province_id = p.id
            WHERE c.province_id = ?
            ORDER BY c.name ASC
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, provinceId);
            try (ResultSet rs = ps.executeQuery()) {
                List<City> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapResultSetToCity(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query cities by provinceId: " + provinceId, e);
        }
    }

    @Override
    public List<City> findAll() {
        String sql = """
            SELECT c.id, c.province_id, c.name, c.created_at,
                   p.code AS province_code, p.name AS province_name, p.created_at AS province_created_at
            FROM cities c
            JOIN provinces p ON c.province_id = p.id
            ORDER BY c.name ASC
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<City> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToCity(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query all cities", e);
        }
    }

    @Override
    public City save(City city) {
        if (city.getId() == null) {
            String sql = "INSERT INTO cities (province_id, name) VALUES (?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, city.getProvinceId());
                ps.setString(2, city.getName());
                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        city.setId(generatedKeys.getLong(1));
                    }
                }
                return city;
            } catch (SQLException e) {
                throw new DataAccessException("Failed to insert city: " + city.getName(), e);
            }
        } else {
            String sql = "UPDATE cities SET province_id = ?, name = ? WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, city.getProvinceId());
                ps.setString(2, city.getName());
                ps.setLong(3, city.getId());
                ps.executeUpdate();
                return city;
            } catch (SQLException e) {
                throw new DataAccessException("Failed to update city with id: " + city.getId(), e);
            }
        }
    }

    private City mapResultSetToCity(ResultSet rs) throws SQLException {
        City c = new City();
        c.setId(rs.getLong("id"));
        c.setProvinceId(rs.getLong("province_id"));
        c.setName(rs.getString("name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }

        Province p = new Province();
        p.setId(rs.getLong("province_id"));
        p.setCode(rs.getString("province_code"));
        p.setName(rs.getString("province_name"));
        Timestamp pTs = rs.getTimestamp("province_created_at");
        if (pTs != null) {
            p.setCreatedAt(pTs.toLocalDateTime());
        }
        c.setProvince(p);

        return c;
    }
}
