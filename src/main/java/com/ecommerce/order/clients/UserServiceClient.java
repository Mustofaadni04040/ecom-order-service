package com.ecommerce.order.clients;

import com.ecommerce.order.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Optional;

@HttpExchange
public interface UserServiceClient {

    @GetExchange("/api/users/{id}")
    Optional<UserResponse> getUserDetails(@PathVariable String id);
}