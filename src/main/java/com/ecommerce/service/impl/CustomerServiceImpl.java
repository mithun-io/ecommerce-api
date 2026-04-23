package com.ecommerce.service.impl;

import com.ecommerce.dto.response.*;
import com.ecommerce.entity.*;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.NoResourceFoundException;
import com.ecommerce.exception.OutOfStockException;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.mapper.OrdersMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.OrdersRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CustomerService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    @Value("${razorpay.api.key}")
    private String razorpayKey;

    @Value("${razorpay.api.secret}")
    private String razorpaySecret;

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrdersRepository ordersRepository;

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final CartItemMapper cartItemMapper;
    private final OrdersMapper ordersMapper;

    private Customer getCustomer(String email) {
        return customerRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("user not found!"));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NoResourceFoundException("product not found"));
    }

    @Transactional
    @Override
    public PageResponse<ProductResponse> getProducts(String name, String category, int page, int size, String sortBy, boolean desc, String lowerRange, String higherRange) {
        Sort sort = desc ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Product> products;
        if (name == null && category == null && lowerRange == null && higherRange == null) {
            products = productRepository.findByApprovedTrue(pageable);
        } else if (lowerRange != null && higherRange != null) {
            products = productRepository.findByPriceBetween(Double.parseDouble(lowerRange), Double.parseDouble(higherRange), pageable);
        } else if (name != null && category != null) {
            products = productRepository.findByNameAndCategory(name, category, pageable);
        } else if (name != null) {
            products = productRepository.findByName(name, pageable);
        } else {
            products = productRepository.findByCategory(category, pageable);
        }

        if (products.isEmpty()) {
            throw new OutOfStockException("products not found");
        }
        List<ProductResponse> productResponses = productMapper.toProductResponses(products.getContent());
        return new PageResponse<>(productResponses, products.getNumber() + 1, products.getSize(), products.getTotalElements(), products.getTotalPages());
    }

    @Transactional
    @Override
    public CartResponse addToCart(Long id, String email, String size, Double price) {
        Product product = getProduct(id);
        Customer customer = getCustomer(email);

        if (product.getStock() <= 0) {
            throw new OutOfStockException("out of stock!");
        }

        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();
            customer.setCart(cart);
        }

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            cart.setCartItems(cartItems);
        }

        boolean found = false;
        for (CartItem cartItem : cartItems) {
            if (Objects.equals(cartItem.getProduct().getId(), product.getId()) && Objects.equals(cartItem.getSize(), size)) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            CartItem cartItem = CartItem.builder()
                    .quantity(1)
                    .size(size)
                    .price(product.getPrice())
                    .product(product)
                    .build();
        }

        product.setStock(product.getStock() - 1);
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    @Override
    public CartResponse removeFromCart(Long productId, String email, String size) {
        Customer customer = getCustomer(email);

        Cart cart = customer.getCart();
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new NoResourceFoundException("cart is empty");
        }

        List<CartItem> cartItems = cart.getCartItems();
        CartItem found = null;
        for (CartItem cartItem : cartItems) {
            if (Objects.equals(cartItem.getProduct().getId(), productId) && Objects.equals(cartItem.getSize(), size)) {
                found = cartItem;
                break;
            }
        }

        if (found == null) {
            throw new NoResourceFoundException("product not found in the cart");
        }

        Product product = found.getProduct();
        if (found.getQuantity() > 1) {
            found.setQuantity(found.getQuantity() - 1);
        } else {
            cartItems.remove(found);
        }
        product.setStock(product.getStock() + 1);
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    @Override
    public List<CartItemResponse> viewCart(String email) {
        Customer customer = getCustomer(email);

        Cart cart = customer.getCart();
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new NoResourceFoundException("cart is empty");
        }
        return cartItemMapper.toCartItemResponses(cart.getCartItems());
    }

    @Transactional
    @Override
    public PaymentResponse buyFromCart(String email, String address) {
        Customer customer = getCustomer(email);

        Cart cart = customer.getCart();
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new NoResourceFoundException("cart is empty");
        }

        List<CartItem> cartItems = cart.getCartItems();
        double amount = cartItems.stream().mapToDouble(x -> x.getQuantity() * x.getPrice()).sum();
        String orderId;

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("currency", "INR");
            jsonObject.put("amount", (int) (amount * 100));

            Order order = razorpayClient.orders.create(jsonObject);
            orderId = order.get("id");

        } catch (RazorpayException e) {
            throw new RuntimeException("payment initialization failed " + PaymentStatus.FAILED);
        }

        Orders orders = Orders.builder()
                .amount(amount)
                .address(address)
                .orderId(orderId)
                .paymentId(null)
                .paymentStatus(PaymentStatus.PENDING)
                .customer(customer)
                .build();

        List<CartItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            CartItem newItem = new CartItem();
            newItem.setQuantity(cartItem.getQuantity());
            newItem.setSize(cartItem.getSize());
            newItem.setPrice(cartItem.getPrice());
            newItem.setProduct(cartItem.getProduct());
            orderItems.add(newItem);
        }
        orders.setCartItems(orderItems);
        ordersRepository.save(orders);

        cart.getCartItems().clear();
        return PaymentResponse.builder()
                .key(razorpayKey)
                .amount(amount * 100)
                .currency("INR")
                .orderId(orderId)
                .name(customer.getUser().getUsername())
                .email(email)
                .mobile(customer.getUser().getMobile())
                .callBackUrl("/customer/confirm-payment/" + orders.getId())
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Transactional
    @Override
    public PaymentResponse confirmPayment(String orderId, String razorpayId) {
        Orders orders = ordersRepository.findByOrderId(orderId).orElseThrow(() -> new NoResourceFoundException("orders not found"));
        orders.setPaymentId(razorpayId);
        orders.setPaymentStatus(PaymentStatus.SUCCESS);
        ordersRepository.save(orders);

        Customer customer = orders.getCustomer();
        return PaymentResponse.builder()
                .name(customer.getUser().getUsername())
                .email(customer.getUser().getEmail())
                .mobile(customer.getUser().getMobile())
                .orderId(orderId)
                .paymentId(razorpayId)
                .key(razorpayKey)
                .amount(orders.getAmount())
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .callBackUrl("/customer/confirm-payment/" + orders.getId())
                .build();
    }

    @Transactional
    @Override
    public List<OrdersResponse> getAllOrders(String email) {
        Customer customer = getCustomer(email);
        List<Orders> orders = ordersRepository.findByCustomer(customer);
        if (orders.isEmpty()) {
            throw new NoResourceFoundException("no orders found!");
        }
        return ordersMapper.toOrderResponses(orders);
    }
}
