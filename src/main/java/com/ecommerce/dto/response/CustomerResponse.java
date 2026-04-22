package com.ecommerce.dto.response;

import com.ecommerce.enums.Gender;
import com.ecommerce.enums.UserRole;
import com.ecommerce.enums.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerResponse {

    private Long id;

    private String name;

    private String email;

    private String mobile;

    private Gender gender;

    private LocalDate dateOfBirth;

    private String address;

    private UserRole customerRole;

    private UserStatus customerStatus;

    private LocalDateTime createdAt;
}
