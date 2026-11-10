package com.agung.registration.dto;

public class RegistrationRequest {
    private String username;
    private String email;
    private String rawPassword;
    private String fullName;
    private Long cityId;

    public RegistrationRequest() {
    }

    public RegistrationRequest(String username, String email, String rawPassword, String fullName, Long cityId) {
        this.username = username;
        this.email = email;
        this.rawPassword = rawPassword;
        this.fullName = fullName;
        this.cityId = cityId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }
}
