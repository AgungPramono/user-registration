package com.agung.registration.dao;

import com.agung.registration.model.City;

import java.util.List;
import java.util.Optional;

public interface CityDao {
    Optional<City> findById(Long id);
    List<City> findByProvinceId(Long provinceId);
    List<City> findAll();
    City save(City city);
}
