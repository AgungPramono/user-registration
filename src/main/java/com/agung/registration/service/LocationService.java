package com.agung.registration.service;

import com.agung.registration.model.City;
import com.agung.registration.model.Province;

import java.util.List;

public interface LocationService {
    Province getProvinceById(Long id);
    List<Province> getAllProvinces();
    City getCityById(Long id);
    List<City> getCitiesByProvinceId(Long provinceId);
    List<City> getAllCities();
    Province createProvince(String code, String name);
    City createCity(Long provinceId, String name);
}
