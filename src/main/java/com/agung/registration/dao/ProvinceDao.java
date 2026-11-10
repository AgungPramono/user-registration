package com.agung.registration.dao;

import com.agung.registration.model.Province;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceDao extends CrudRepository<Province, Long> {
    Optional<Province> findByCode(String code);
    List<Province> findAll();
}
