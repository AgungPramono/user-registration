package com.agung.registration.dao.springjdbc;

import com.agung.registration.dao.CityDao;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
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
public class CityDaoSpringJdbc implements CityDao {

    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert simpleInsert;

    private static final String BASE_SELECT = """
        SELECT c.id, c.province_id, c.name, c.created_at,
               p.code AS province_code, p.name AS province_name, p.created_at AS province_created_at
        FROM cities c
        JOIN provinces p ON c.province_id = p.id
        """;

    private static final RowMapper<City> ROW_MAPPER = (rs, rowNum) -> {
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
    };

    public CityDaoSpringJdbc(DataSource dataSource) {
        this.jdbcClient = JdbcClient.create(dataSource);
        this.simpleInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("cities")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<City> findById(Long id) {
        return jdbcClient.sql(BASE_SELECT + " WHERE c.id = :id")
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    @Override
    public List<City> findByProvinceId(Long provinceId) {
        return jdbcClient.sql(BASE_SELECT + " WHERE c.province_id = :provinceId ORDER BY c.name ASC")
                .param("provinceId", provinceId)
                .query(ROW_MAPPER)
                .list();
    }

    @Override
    public List<City> findAll() {
        return jdbcClient.sql(BASE_SELECT + " ORDER BY c.name ASC")
                .query(ROW_MAPPER)
                .list();
    }

    @Override
    public City save(City city) {
        if (city.getId() == null) {
            Number newId = simpleInsert.executeAndReturnKey(new MapSqlParameterSource()
                    .addValue("province_id", city.getProvinceId())
                    .addValue("name", city.getName()));
            city.setId(newId.longValue());
            return city;
        } else {
            jdbcClient.sql("UPDATE cities SET province_id = :provinceId, name = :name WHERE id = :id")
                    .param("provinceId", city.getProvinceId())
                    .param("name", city.getName())
                    .param("id", city.getId())
                    .update();
            return city;
        }
    }
}
