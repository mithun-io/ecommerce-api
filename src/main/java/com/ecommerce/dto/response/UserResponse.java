package com.ecommerce.dto.response;

import com.ecommerce.enums.Gender;
import com.ecommerce.enums.UserRole;
import com.ecommerce.enums.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String mobile;

    private LocalDate dateOfBirth;

    private String address;

    private Gender gender;

    private UserRole userRole;

    private UserStatus userStatus;

    private LocalDateTime createdAt;
}
