package com.agung.registration.facade;

import com.agung.registration.dto.RegistrationRequest;
import com.agung.registration.dto.RegistrationResponse;
import com.agung.registration.dto.UserDetailDto;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;

import java.util.List;

public interface RegistrationFacade {
    RegistrationResponse register(RegistrationRequest request);
    UserDetailDto getUserProfile(Long userId);
    List<Province> getAvailableProvinces();
    List<City> getCitiesByProvince(Long provinceId);
}
