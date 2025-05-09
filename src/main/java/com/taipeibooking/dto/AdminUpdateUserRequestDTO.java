package com.taipeibooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminUpdateUserRequestDTO {
    @NotBlank(message = "名稱不能為空")
    private String name;

    @NotBlank(message = "Email不能為空")
    @Email(message = "Email格式不正確")
    private String email;

    @Size(min = 4, message = "密碼長度至少需要4個字元")
    private String password;

    private String role;
    private Integer age;
    private String gender;

    public AdminUpdateUserRequestDTO() {
    }

    public AdminUpdateUserRequestDTO(String name, String email, String password, String role, Integer age, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.age = age;
        this.gender = gender;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
