package com.ecommerce.mapper;

import com.ecommerce.dto.request.CustomerRequest;
import com.ecommerce.dto.response.CustomerResponse;
import com.ecommerce.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toCustomer(CustomerRequest customerRequest);

    @Mapping(target = "address", expression = "java(customer.getUser().getAddress())")
    @Mapping(target = "name", expression = "java(customer.getUser().getUsername())")
    @Mapping(target = "mobile", expression = "java(customer.getUser().getMobile())")
    @Mapping(target = "email", expression = "java(customer.getUser().getEmail())")
    @Mapping(target = "gender", expression = "java(customer.getUser().getGender())")
    @Mapping(target = "dateOfBirth", expression = "java(customer.getUser().getDateOfBirth())")
    @Mapping(target = "customerRole", expression = "java(customer.getUser().getUserRole())")
    @Mapping(target = "customerStatus", expression = "java(customer.getUser().getUserStatus())")
    @Mapping(target = "createdAt", expression = "java(customer.getUser().getCreatedAt())")
    CustomerResponse toCustomerResponse(Customer customer);

    List<CustomerResponse> toCustomerResponses(List<Customer> customers);
}
