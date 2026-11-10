package com.agung.registration;

import com.agung.registration.config.DatabaseConfig;
import com.agung.registration.config.SchemaInitializer;
import com.agung.registration.dao.CityDao;
import com.agung.registration.dao.ProvinceDao;
import com.agung.registration.dao.UserDao;
import com.agung.registration.dao.jdbc.CityDaoJdbc;
import com.agung.registration.dao.jdbc.ProvinceDaoJdbc;
import com.agung.registration.dao.jdbc.UserDaoJdbc;
import com.agung.registration.dto.RegistrationRequest;
import com.agung.registration.dto.RegistrationResponse;
import com.agung.registration.dto.UserDetailDto;
import com.agung.registration.exception.DuplicateResourceException;
import com.agung.registration.exception.ValidationException;
import com.agung.registration.facade.RegistrationFacade;
import com.agung.registration.facade.impl.RegistrationFacadeImpl;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
import com.agung.registration.service.LocationService;
import com.agung.registration.service.UserService;
import com.agung.registration.service.impl.LocationServiceImpl;
import com.agung.registration.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegistrationFacadeTest {

    private static RegistrationFacade registrationFacade;
    private static DatabaseConfig dbConfig;

    @BeforeAll
    static void setUp() {
        dbConfig = DatabaseConfig.getInstance();
        DataSource dataSource = dbConfig.getDataSource();

        ProvinceDao provinceDao = new ProvinceDaoJdbc(dataSource);
        CityDao cityDao = new CityDaoJdbc(dataSource);
        UserDao userDao = new UserDaoJdbc(dataSource);

        SchemaInitializer initializer = new SchemaInitializer(dataSource, provinceDao, cityDao);
        initializer.initialize();

        LocationService locationService = new LocationServiceImpl(provinceDao, cityDao);
        UserService userService = new UserServiceImpl(userDao);

        registrationFacade = new RegistrationFacadeImpl(userService, locationService);
    }

    @AfterAll
    static void tearDown() {
        if (dbConfig != null) {
            dbConfig.close();
        }
    }

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
