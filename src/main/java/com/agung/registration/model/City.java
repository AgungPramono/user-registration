package com.agung.registration.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Table("cities")
public class City {
    @Id
    private Long id;
    @Column("province_id")
    private Long provinceId;
    @Transient
    private Province province;
    private String name;
    private LocalDateTime createdAt;

    public City() {
    }

    public City(Long id, Long provinceId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.provinceId = provinceId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public City(Long provinceId, String name) {
        this.provinceId = provinceId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
        if (province != null && province.getId() != null) {
            this.provinceId = province.getId();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City city)) return false;
        return Objects.equals(id, city.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", provinceId=" + provinceId +
                ", name='" + name + '\'' +
                '}';
    }
}
