package com.taipeibooking.dto;

public class UserAdminViewDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Integer age;
    private String gender;

    public UserAdminViewDTO() {
    }

    public UserAdminViewDTO(Long id, String name, String email, String role, Integer age, String gender) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.age = age;
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
