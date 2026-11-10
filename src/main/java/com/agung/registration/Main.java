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
import com.agung.registration.exception.AppException;
import com.agung.registration.facade.RegistrationFacade;
import com.agung.registration.facade.impl.RegistrationFacadeImpl;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
import com.agung.registration.service.LocationService;
import com.agung.registration.service.UserService;
import com.agung.registration.service.impl.LocationServiceImpl;
import com.agung.registration.service.impl.UserServiceImpl;

import javax.sql.DataSource;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        DataSource dataSource = dbConfig.getDataSource();

        try {
            ProvinceDao provinceDao = new ProvinceDaoJdbc(dataSource);
            CityDao cityDao = new CityDaoJdbc(dataSource);
            UserDao userDao = new UserDaoJdbc(dataSource);

            SchemaInitializer initializer = new SchemaInitializer(dataSource, provinceDao, cityDao);
            initializer.initialize();

            LocationService locationService = new LocationServiceImpl(provinceDao, cityDao);
            UserService userService = new UserServiceImpl(userDao);

            RegistrationFacade registrationFacade = new RegistrationFacadeImpl(userService, locationService);

            System.out.println("=== Master Data Wilayah ===");
            List<Province> provinces = registrationFacade.getAvailableProvinces();
            for (Province p : provinces) {
                System.out.println("- Provinsi: " + p.getName() + " (" + p.getCode() + ")");
                List<City> cities = registrationFacade.getCitiesByProvince(p.getId());
                for (City c : cities) {
                    System.out.println("   * Kota [ID=" + c.getId() + "]: " + c.getName());
                }
            }

            System.out.println("\n=== Registrasi User Baru ===");
            Long sampleCityId = provinces.getFirst().getId();
            List<City> dkiCities = registrationFacade.getCitiesByProvince(sampleCityId);
            Long targetCityId = dkiCities.getFirst().getId();

            String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 10000);
            RegistrationRequest request = new RegistrationRequest(
                    "budi_" + uniqueSuffix,
                    "budi_" + uniqueSuffix + "@example.com",
                    "Secret123!",
                    "Budi Santoso",
                    targetCityId
            );

            RegistrationResponse response = registrationFacade.register(request);
            System.out.println("Registrasi Berhasil!");
            System.out.println("User ID   : " + response.getUserId());
            System.out.println("Username  : " + response.getUsername());
            System.out.println("Email     : " + response.getEmail());
            System.out.println("Nama      : " + response.getFullName());
            System.out.println("Kota      : " + response.getCityName());
            System.out.println("Provinsi  : " + response.getProvinceName());
            System.out.println("Status    : " + response.getStatus());

            System.out.println("\n=== Detail Profil User Terelasi ===");
            UserDetailDto userDetail = registrationFacade.getUserProfile(response.getUserId());
            System.out.println("Detail User: " + userDetail);

            System.out.println("\n=== Uji Validasi Duplikasi Registrasi ===");
            try {
                registrationFacade.register(request);
            } catch (AppException e) {
                System.out.println("Validasi tertangkap sesuai harapan: " + e.getMessage());
            }

        } finally {
            dbConfig.close();
        }
    }
}
