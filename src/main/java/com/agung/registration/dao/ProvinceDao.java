package com.agung.registration.dao;

import com.agung.registration.model.Province;

import java.util.List;
import java.util.Optional;

public interface ProvinceDao {
    Optional<Province> findById(Long id);
    Optional<Province> findByCode(String code);
    List<Province> findAll();
    Province save(Province province);
}
