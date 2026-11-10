package com.agung.registration.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Province {
    private Long id;
    private String code;
    private String name;
    private LocalDateTime createdAt;

    public Province() {
    }

    public Province(Long id, String code, String name, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Province(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
        if (!(o instanceof Province province)) return false;
        return Objects.equals(id, province.id) || Objects.equals(code, province.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Province{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
