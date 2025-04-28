package com.taipeibooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminUpdateUserRequestDTO {

    @NotBlank(message = "姓名不得為空")
    @Size(max = 50, message = "姓名長度不能超過 50")
    private String name;

    @NotBlank(message = "Email 不得為空")
    @Email(message = "Email 格式無效")
    @Size(max = 100, message = "Email 長度不能超過 100")
    private String email;

    @NotBlank(message = "角色不得為空")
    private String role;

     public String getName() { return name; }
     public void setName(String name) { this.name = name; }
     public String getEmail() { return email; }
     public void setEmail(String email) { this.email = email; }
     public String getRole() { return role; }
     public void setRole(String role) { this.role = role; }
}
