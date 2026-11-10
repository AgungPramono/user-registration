package com.agung.registration.service.impl;

import com.agung.registration.dao.CityDao;
import com.agung.registration.dao.ProvinceDao;
import com.agung.registration.exception.ResourceNotFoundException;
import com.agung.registration.exception.ValidationException;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
import com.agung.registration.service.LocationService;

import java.util.List;

public class LocationServiceImpl implements LocationService {

    private final ProvinceDao provinceDao;
    private final CityDao cityDao;

    public LocationServiceImpl(ProvinceDao provinceDao, CityDao cityDao) {
        this.provinceDao = provinceDao;
        this.cityDao = cityDao;
    }

    @Override
    public Province getProvinceById(Long id) {
        if (id == null) {
            throw new ValidationException("Province ID cannot be null");
        }
        return provinceDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Province with id " + id + " not found"));
    }

    @Override
    public List<Province> getAllProvinces() {
        return provinceDao.findAll();
    }

    @Override
    public City getCityById(Long id) {
        if (id == null) {
            throw new ValidationException("City ID cannot be null");
        }
        return cityDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City with id " + id + " not found"));
    }

    @Override
    public List<City> getCitiesByProvinceId(Long provinceId) {
        if (provinceId == null) {
            throw new ValidationException("Province ID cannot be null");
        }
        return cityDao.findByProvinceId(provinceId);
    }

    @Override
    public List<City> getAllCities() {
        return cityDao.findAll();
    }

    @Override
    public Province createProvince(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Province code is required");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Province name is required");
        }
        Province province = new Province(code.trim().toUpperCase(), name.trim());
        return provinceDao.save(province);
    }

    @Override
    public City createCity(Long provinceId, String name) {
        if (provinceId == null) {
            throw new ValidationException("Province ID is required");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("City name is required");
        }
        // Verify province exists
        getProvinceById(provinceId);

        City city = new City(provinceId, name.trim());
        return cityDao.save(city);
    }
}
