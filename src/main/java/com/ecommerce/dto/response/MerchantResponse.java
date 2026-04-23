package com.ecommerce.dto.response;

import com.ecommerce.entity.User;
import com.ecommerce.enums.Gender;
import com.ecommerce.enums.UserRole;
import com.ecommerce.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MerchantResponse {

    private Long id;

    private String businessName;

    private String ownerName;

    private String businessEmail;

    private String businessMobile;

    private String businessAddress;

    private Gender gender;

    private LocalDate dateOfBirth;

    private String gstNumber;

    private String panNumber;

    private String businessLicense;

    private UserStatus merchantStatus;

    private UserRole merchantRole;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm a")
    private LocalDateTime createdAt;

    private User user;
}
