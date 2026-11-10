package com.agung.registration;

import com.agung.registration.config.AppConfig;
import com.agung.registration.dto.RegistrationRequest;
import com.agung.registration.dto.RegistrationResponse;
import com.agung.registration.dto.UserDetailDto;
import com.agung.registration.exception.ValidationException;
import com.agung.registration.facade.RegistrationFacade;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegistrationFacadeTest {

    @Autowired
    private RegistrationFacade registrationFacade;

    @Test
    @Order(1)
    void shouldFetchMasterProvincesAndCities() {
        List<Province> provinces = registrationFacade.getAvailableProvinces();
        assertFalse(provinces.isEmpty(), "Provinces should not be empty");

        Province first = provinces.getFirst();
        List<City> cities = registrationFacade.getCitiesByProvince(first.getId());
        assertFalse(cities.isEmpty(), "Cities for first province should not be empty");
    }

    @Test
    @Order(2)
    void shouldRegisterNewUserSuccessfully() {
        List<Province> provinces = registrationFacade.getAvailableProvinces();
        City city = registrationFacade.getCitiesByProvince(provinces.getFirst().getId()).getFirst();

        String suffix = String.valueOf(System.currentTimeMillis());
        RegistrationRequest req = new RegistrationRequest(
                "user_" + suffix.substring(suffix.length() - 6),
                "test_" + suffix + "@mail.com",
                "password123",
                "Integration Tester",
                city.getId()
        );

        RegistrationResponse res = registrationFacade.register(req);
        assertNotNull(res.getUserId());
        assertEquals(req.getUsername(), res.getUsername());
        assertEquals(req.getEmail(), res.getEmail());
        assertEquals(city.getName(), res.getCityName());

        UserDetailDto profile = registrationFacade.getUserProfile(res.getUserId());
        assertEquals(res.getUserId(), profile.getId());
        assertEquals(city.getName(), profile.getCityName());
        assertNotNull(profile.getProvinceName());
    }

    @Test
    @Order(3)
    void shouldThrowValidationExceptionWhenInvalidInput() {
        RegistrationRequest invalid = new RegistrationRequest(
                "ab", // too short
                "invalid-email",
                "123", // too short
                "",
                999999L
        );

        assertThrows(ValidationException.class, () -> registrationFacade.register(invalid));
    }
}
