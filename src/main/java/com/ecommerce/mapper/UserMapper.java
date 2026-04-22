package com.ecommerce.mapper;

import com.ecommerce.dto.request.UserRequest;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserRequest userRequest);

    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponses(List<User> users);
}
