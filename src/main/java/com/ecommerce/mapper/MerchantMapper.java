package com.ecommerce.mapper;

import com.ecommerce.dto.request.MerchantRequest;
import com.ecommerce.dto.response.MerchantResponse;
import com.ecommerce.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MerchantMapper {

    Merchant toMerchant(MerchantRequest merchantRequest);

    @Mapping(target = "createdAt", expression = "java(merchant.getUser().getCreatedAt())")
    @Mapping(target = "businessName", expression = "java(merchant.getBusinessName())")
    @Mapping(target = "ownerName", expression = "java(merchant.getUser().getUsername())")
    @Mapping(target = "businessEmail", expression = "java(merchant.getUser().getEmail())")
    @Mapping(target = "businessMobile", expression = "java(merchant.getUser().getMobile())")
    @Mapping(target = "businessAddress", expression = "java(merchant.getUser().getAddress())")
    @Mapping(target = "gender", expression = "java(merchant.getUser().getGender())")
    @Mapping(target = "dateOfBirth", expression = "java(merchant.getUser().getDateOfBirth())")
    @Mapping(target = "merchantRole", expression = "java(merchant.getUser().getUserRole())")
    @Mapping(target = "merchantStatus", expression = "java(merchant.getUser().getUserStatus())")
    MerchantResponse toMerchantResponse(Merchant merchant);

    List<MerchantResponse> toMerchantResponses(List<Merchant> merchants);
}
