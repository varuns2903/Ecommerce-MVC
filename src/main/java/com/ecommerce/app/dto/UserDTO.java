package com.ecommerce.app.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String address;

    public UserDTO(String userId, String name, String email, String phone, String address) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
