package com.ecommerce.dto.request;

import com.ecommerce.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MerchantRequest {

    @NotBlank(message = "business name is required")
    private String businessName;

    @NotBlank(message = "owner name is required")
    private String ownerName;

    @Email(message = "invalid email format")
    @NotBlank(message = "business email is required")
    private String businessEmail;

    @NotBlank(message = "business mobile is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "business mobile must be 10 digits")
    private String businessMobile;

    @NotBlank(message = "business address is required")
    private String businessAddress;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$", message = "password must contain uppercase, lowercase, number and special character")
    private String password;

    @NotNull(message = "gender is required")
    private Gender gender;

    @NotNull(message = "date of birth is required")
    @Past(message = "date of birth must be past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "gst number is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "invalid gst number")
    private String gstNumber;

    @NotBlank(message = "pan number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "invalid pan number")
    private String panNumber;

    @NotBlank(message = "business license is required")
    private String businessLicense;
}