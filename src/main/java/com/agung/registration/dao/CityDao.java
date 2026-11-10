package com.agung.registration.dao;

import com.agung.registration.model.City;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityDao extends CrudRepository<City, Long> {
    List<City> findByProvinceId(Long provinceId);
    List<City> findAll();
}
