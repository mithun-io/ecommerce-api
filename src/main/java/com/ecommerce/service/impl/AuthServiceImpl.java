package com.ecommerce.service.impl;

import com.ecommerce.dto.request.*;
import com.ecommerce.dto.response.LoginResponse;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Merchant;
import com.ecommerce.entity.User;
import com.ecommerce.enums.UserRole;
import com.ecommerce.enums.UserStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.NoResourceFoundException;
import com.ecommerce.helper.CustomUserDetails;
import com.ecommerce.helper.CustomUserDetailsService;
import com.ecommerce.helper.EmailService;
import com.ecommerce.helper.RedisService;
import com.ecommerce.mapper.CustomerMapper;
import com.ecommerce.mapper.MerchantMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.MerchantRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

    private final UserMapper userMapper;
    private final CustomerMapper customerMapper;
    private final MerchantMapper merchantMapper;

    private final CustomUserDetailsService customUserDetailsService;
    private final RedisService redisService;
    private final EmailService emailService;

    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private Integer generateOtp() {
        return secureRandom.nextInt(100000, 1000000);
    }

    @Transactional
    @Override
    public void customerRegistration(CustomerRequest customerRequest) {
        if (userRepository.existsByEmailAndMobile(customerRequest.getEmail(), customerRequest.getMobile())) {
            throw new ConflictException("user already exists");
        }
        if (redisService.isPendingCustomerExists(customerRequest.getEmail())) {
            throw new ConflictException("pending registration already exists");
        }

        int otp = generateOtp();
        emailService.sendOtp(customerRequest.getName(), customerRequest.getEmail(), otp);
        redisService.storeOtp(customerRequest.getEmail(), otp);
        redisService.storePendingCustomer(customerRequest.getEmail(), customerRequest);
    }

    @Transactional
    @Override
    public void merchantRegistration(MerchantRequest merchantRequest) {
        if (userRepository.existsByEmailAndMobile(merchantRequest.getBusinessEmail(), merchantRequest.getBusinessMobile())) {
            throw new ConflictException("user already exists");
        }
        if (redisService.isPendingMerchantExists(merchantRequest.getBusinessEmail())) {
            throw new ConflictException("pending registration already exists");
        }

        int otp = generateOtp();
        emailService.sendOtp(merchantRequest.getBusinessEmail(), merchantRequest.getBusinessMobile(), otp);
        redisService.storeOtp(merchantRequest.getBusinessEmail(), otp);
        redisService.storePendingMerchant(merchantRequest.getBusinessEmail(), merchantRequest);
    }

    @Transactional
    @Override
    public void verifyCustomerOtp(OtpRequest otpRequest) {
        Integer storedOtp = redisService.getOtp(otpRequest.getEmail());
        CustomerRequest storedCustomer = redisService.getPendingCustomer(otpRequest.getEmail());

        if (storedOtp == null) {
            throw new BadRequestException("otp expired or invalid");
        }
        if (storedCustomer == null) {
            throw new BadRequestException("no pending registration found");
        }
        if (!String.valueOf(storedOtp).equals(otpRequest.getOtp())) {
            throw new BadRequestException("invalid otp");
        }
        if (userRepository.existsByEmail(storedCustomer.getEmail())) {
            throw new ConflictException("user already exists!");
        }

        User user = User.builder()
                .username(storedCustomer.getName())
                .email(storedCustomer.getEmail())
                .mobile(storedCustomer.getMobile())
                .dateOfBirth(storedCustomer.getDateOfBirth())
                .address(storedCustomer.getAddress())
                .password(passwordEncoder.encode(storedCustomer.getPassword()))
                .gender(storedCustomer.getGender())
                .userRole(UserRole.CUSTOMER)
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        Customer customer = Customer.builder()
                .user(user)
                .build();
        customerRepository.save(customer);

        emailService.sendConfirmation(user.getUsername(), user.getEmail(), user.getPassword());
        redisService.deleteOtp(otpRequest.getEmail());
        redisService.deletePendingCustomer(otpRequest.getEmail());
    }

    @Transactional
    @Override
    public void verifyMerchantOtp(OtpRequest otpRequest) {
        Integer storedOtp = redisService.getOtp(otpRequest.getEmail());
        MerchantRequest storedMerchant = redisService.getPendingMerchant(otpRequest.getEmail());

        if (storedOtp == null) {
            throw new BadRequestException("otp expired or invalid");
        }
        if (storedMerchant == null) {
            throw new BadRequestException("no pending registration found");
        }
        if (!String.valueOf(storedOtp).equals(otpRequest.getOtp())) {
            throw new BadRequestException("invalid otp");
        }
        if (userRepository.existsByEmail(storedMerchant.getBusinessEmail())) {
            throw new ConflictException("user already exists!");
        }

        User user = User.builder()
                .username(storedMerchant.getOwnerName())
                .email(storedMerchant.getBusinessEmail())
                .mobile(storedMerchant.getBusinessMobile())
                .dateOfBirth(storedMerchant.getDateOfBirth())
                .address(storedMerchant.getBusinessAddress())
                .password(storedMerchant.getPassword())
                .gender(storedMerchant.getGender())
                .userRole(UserRole.MERCHANT)
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        Merchant merchant = Merchant.builder()
                .businessName(storedMerchant.getBusinessName())
                .gstNumber(storedMerchant.getGstNumber())
                .panNumber(storedMerchant.getPanNumber())
                .businessLicense(storedMerchant.getBusinessLicense())
                .build();
        merchantRepository.save(merchant);

        emailService.sendConfirmation(user.getUsername(), user.getEmail(), user.getPassword());
        redisService.deleteOtp(otpRequest.getEmail());
        redisService.deletePendingMerchant(otpRequest.getEmail());
    }

    @Transactional
    @Override
    public void resendOtp(String email) {

        CustomerRequest customer = redisService.getPendingCustomer(email);
        MerchantRequest merchant = redisService.getPendingMerchant(email);

        if (customer == null && merchant == null) {
            throw new BadRequestException("no pending registration found");
        }

        redisService.deleteOtp(email);

        int otp = generateOtp();

        if (customer != null) {
            emailService.sendOtp(customer.getName(), customer.getEmail(), otp);
            redisService.storeOtp(customer.getEmail(), otp);
        } else {
            emailService.sendOtp(merchant.getOwnerName(), merchant.getBusinessEmail(), otp);
            redisService.storeOtp(merchant.getBusinessEmail(), otp);
        }
    }

    @Transactional
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new NoResourceFoundException("user not found"));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(loginRequest.getEmail());
        String token = jwtUtil.generateToken(customUserDetails);
        log.info("{} logged in successfully", user.getUsername());

        UserResponse userResponse = userMapper.toUserResponse(user);
        System.out.println(user);
        return new LoginResponse(token, userResponse);
    }

    @Transactional
    @Override
    public UserResponse passwordChange(PasswordChangeRequest passwordChangeRequest, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("user not found"));

        if (!passwordEncoder.matches(passwordChangeRequest.getPreviousPassword(), user.getPassword())) {
            throw new BadRequestException("password is incorrect");
        }

        if (passwordEncoder.matches(passwordChangeRequest.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("new password cannot be same as previous password");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    @Override
    public void forgetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("user not found"));

        int otp = generateOtp();
        emailService.sendOtp(user.getUsername(), email, otp);
        redisService.storeOtp(email, otp);
    }

    @Transactional
    @Override
    public void passwordReset(PasswordResetRequest passwordResetRequest) {
        User user = userRepository.findByEmail(passwordResetRequest.getEmail()).orElseThrow(() -> new NoResourceFoundException("user not found"));

        Integer storedOtp = redisService.getOtp(passwordResetRequest.getOtp());
        if (storedOtp == null) {
            throw new BadRequestException("otp expired or invalid");
        }
        if (!String.valueOf(storedOtp).equals(passwordResetRequest.getOtp())) {
            throw new BadRequestException("invalid otp");
        }
        if (passwordEncoder.matches(passwordResetRequest.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("new password cannot be same as previous password");
        }

        user.setPassword(passwordEncoder.encode(passwordResetRequest.getNewPassword()));
        userRepository.save(user);

        emailService.sendConfirmation(user.getUsername(), user.getEmail(), user.getPassword());
        redisService.deleteOtp(passwordResetRequest.getEmail());
    }
}
