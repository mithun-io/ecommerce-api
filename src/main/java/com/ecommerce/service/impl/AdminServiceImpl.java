package com.ecommerce.service.impl;

import com.ecommerce.dto.response.CustomerResponse;
import com.ecommerce.dto.response.MerchantResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Merchant;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.enums.ProductStatus;
import com.ecommerce.enums.UserRole;
import com.ecommerce.enums.UserStatus;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.NoResourceFoundException;
import com.ecommerce.kafka.event.ProductEvent;
import com.ecommerce.kafka.producer.ProducerService;
import com.ecommerce.mapper.CustomerMapper;
import com.ecommerce.mapper.MerchantMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.MerchantRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final MerchantMapper merchantMapper;

    private final ProducerService producerService;

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoResourceFoundException("user not found"));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NoResourceFoundException("product not found"));
    }

    private void validateUserTransition(UserStatus currentStatus, UserStatus target) {
        if (currentStatus == target) {
            throw new ConflictException("user already in " + currentStatus + " state");
        }

        boolean isValid = switch (currentStatus) {
            case ACTIVE -> target == UserStatus.INACTIVE || target == UserStatus.BLOCKED;
            case INACTIVE, BLOCKED -> target == UserStatus.ACTIVE;
            default -> throw new IllegalStateException("unexpected status: " + currentStatus);
        };

        if (!isValid) {
            throw new IllegalArgumentException("invalid status transition");
        }
    }

    private void validateProductTransition(ProductStatus currentStatus, ProductStatus target) {
        if (currentStatus == target) {
            throw new ConflictException("product already in " + currentStatus + " state");
        }

        boolean isValid = switch (currentStatus) {
            case PENDING -> target == ProductStatus.APPROVED || target == ProductStatus.REJECTED;
            case APPROVED, REJECTED -> false;
            default -> throw new IllegalStateException("unexpected status: " + currentStatus);
        };

        if (!isValid) {
            throw new IllegalArgumentException("invalid product status transition");
        }
    }

    @Transactional
    @Override
    public UserResponse blockUser(Long id) {
        User user = getUser(id);
        if (user.getUserRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("admin cannot be blocked!");
        }
        validateUserTransition(user.getUserStatus(), UserStatus.BLOCKED);
        user.setUserStatus(UserStatus.BLOCKED);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    @Override
    public UserResponse unblockUser(Long id) {
        User user = getUser(id);
        validateUserTransition(user.getUserStatus(), UserStatus.ACTIVE);
        user.setUserStatus(UserStatus.ACTIVE);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    @Override
    public ProductResponse approveProduct(Long id) {
        Product product = getProduct(id);
        validateProductTransition(product.getProductStatus(), ProductStatus.APPROVED);
        product.setProductStatus(ProductStatus.APPROVED);

        ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_APPROVED")
                .productId(product.getId())
                .merchantId(product.getMerchant().getId())
                .merchantEmail(
                        product.getMerchant() != null && product.getMerchant().getUser() != null
                                ? product.getMerchant().getUser().getEmail()
                                : null
                )
                .title(product.getName())
                .price(product.getPrice())
                .status(product.getProductStatus().name())
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();

        producerService.sendProductEvent(event);
        return productMapper.toProductResponse(product);
    }

    @Transactional
    @Override
    public ProductResponse denyProduct(Long id) {
        Product product = getProduct(id);
        validateProductTransition(product.getProductStatus(), ProductStatus.REJECTED);
        product.setProductStatus(ProductStatus.REJECTED);
        productRepository.save(product);

        ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_REJECTED")
                .productId(product.getId())
                .merchantId(product.getMerchant().getId())
                .merchantEmail(product.getMerchant().getUser().getEmail())
                .title(product.getName())
                .status(product.getProductStatus().name())
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();

        producerService.sendProductEvent(event);
        return productMapper.toProductResponse(product);
    }

    @Transactional
    @Override
    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            throw new NoResourceFoundException("no customers found");
        }
        return customerMapper.toCustomerResponses(customers);
    }

    @Transactional
    @Override
    public List<MerchantResponse> getAllMerchants() {
        List<Merchant> merchants = merchantRepository.findAll();
        if (merchants.isEmpty()) {
            throw new NoResourceFoundException("merchants not found");
        }
        return merchantMapper.toMerchantResponses(merchants);
    }

    @Transactional
    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            throw new NoResourceFoundException("products not found");
        }
        return productMapper.toProductResponses(products);
    }
}
