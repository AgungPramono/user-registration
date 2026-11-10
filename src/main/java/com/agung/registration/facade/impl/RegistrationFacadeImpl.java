package com.agung.registration.facade.impl;

import com.agung.registration.dto.RegistrationRequest;
import com.agung.registration.dto.RegistrationResponse;
import com.agung.registration.dto.UserDetailDto;
import com.agung.registration.exception.ResourceNotFoundException;
import com.agung.registration.exception.ValidationException;
import com.agung.registration.facade.RegistrationFacade;
import com.agung.registration.model.City;
import com.agung.registration.model.Province;
import com.agung.registration.model.User;
import com.agung.registration.service.LocationService;
import com.agung.registration.service.UserService;
import com.agung.registration.util.PasswordHasher;

import java.util.List;
import java.util.regex.Pattern;

public class RegistrationFacadeImpl implements RegistrationFacade {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private final UserService userService;
    private final LocationService locationService;

    public RegistrationFacadeImpl(UserService userService, LocationService locationService) {
        this.userService = userService;
        this.locationService = locationService;
    }

    @Override
    public RegistrationResponse register(RegistrationRequest request) {
        validateRegistrationRequest(request);

        City city = locationService.getCityById(request.getCityId());

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(PasswordHasher.hash(request.getRawPassword()));
        user.setFullName(request.getFullName().trim());
        user.setCityId(city.getId());
        user.setStatus("ACTIVE");

        User savedUser = userService.createUser(user);

        return new RegistrationResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                city.getName(),
                city.getProvince() != null ? city.getProvince().getName() : "Unknown",
                savedUser.getStatus(),
                savedUser.getCreatedAt()
        );
    }

    @Override
    public UserDetailDto getUserProfile(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        UserDetailDto dto = new UserDetailDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setCityId(user.getCityId());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        if (user.getCity() != null) {
            dto.setCityName(user.getCity().getName());
            if (user.getCity().getProvince() != null) {
                dto.setProvinceId(user.getCity().getProvince().getId());
                dto.setProvinceCode(user.getCity().getProvince().getCode());
                dto.setProvinceName(user.getCity().getProvince().getName());
            }
        }

        return dto;
    }

    @Override
    public List<Province> getAvailableProvinces() {
        return locationService.getAllProvinces();
    }

    @Override
    public List<City> getCitiesByProvince(Long provinceId) {
        return locationService.getCitiesByProvinceId(provinceId);
    }

    private void validateRegistrationRequest(RegistrationRequest request) {
        if (request == null) {
            throw new ValidationException("Registration request cannot be null");
        }
        if (request.getUsername() == null || !USERNAME_PATTERN.matcher(request.getUsername().trim()).matches()) {
            throw new ValidationException("Username must be alphanumeric and between 3-30 characters");
        }
        if (request.getEmail() == null || !EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            throw new ValidationException("Email is invalid");
        }
        if (request.getRawPassword() == null || request.getRawPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
        if (request.getFullName() == null || request.getFullName().trim().isBlank()) {
            throw new ValidationException("Full name is required");
        }
        if (request.getCityId() == null) {
            throw new ValidationException("City selection is required");
        }
    }
}
