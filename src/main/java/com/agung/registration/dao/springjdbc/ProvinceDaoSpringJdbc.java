package com.agung.registration.dao.springjdbc;

import com.agung.registration.dao.ProvinceDao;
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
public class ProvinceDaoSpringJdbc implements ProvinceDao {

    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert simpleInsert;

    private static final RowMapper<Province> ROW_MAPPER = (rs, rowNum) -> {
        Province p = new Province();
        p.setId(rs.getLong("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    };

    public ProvinceDaoSpringJdbc(DataSource dataSource) {
        this.jdbcClient = JdbcClient.create(dataSource);
        this.simpleInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("provinces")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Province> findById(Long id) {
        return jdbcClient.sql("SELECT id, code, name, created_at FROM provinces WHERE id = :id")
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    @Override
    public Optional<Province> findByCode(String code) {
        return jdbcClient.sql("SELECT id, code, name, created_at FROM provinces WHERE code = :code")
                .param("code", code)
                .query(ROW_MAPPER)
                .optional();
    }

    @Override
    public List<Province> findAll() {
        return jdbcClient.sql("SELECT id, code, name, created_at FROM provinces ORDER BY name ASC")
                .query(ROW_MAPPER)
                .list();
    }

    @Override
    public Province save(Province province) {
        if (province.getId() == null) {
            Number newId = simpleInsert.executeAndReturnKey(new MapSqlParameterSource()
                    .addValue("code", province.getCode())
                    .addValue("name", province.getName()));
            province.setId(newId.longValue());
            return province;
        } else {
            jdbcClient.sql("UPDATE provinces SET code = :code, name = :name WHERE id = :id")
                    .param("code", province.getCode())
                    .param("name", province.getName())
                    .param("id", province.getId())
                    .update();
            return province;
        }
    }
}
