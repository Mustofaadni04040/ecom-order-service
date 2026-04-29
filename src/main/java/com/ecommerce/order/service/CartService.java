package com.ecommerce.order.service;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.model.AddToCartResult;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "productService")
    public AddToCartResult addToCart(String userId, CartItemRequest request) {
        ProductResponse productResponse = productServiceClient.getProductDetails(request.getProductId());
        var userResponse = userServiceClient.getUserDetails(userId);

        if (productResponse == null) {
            return AddToCartResult.PRODUCT_NOT_FOUND;
        }

        if (productResponse.getStockQuantity() < request.getQuantity()) {
            return AddToCartResult.OUT_OF_STOCK;
        }

        if (userResponse.isEmpty()) {
            return AddToCartResult.USER_NOT_FOUND;
        }

        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());

        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
            existingCartItem.setPrice(BigDecimal.valueOf(1000.000));
            cartItemRepository.save(existingCartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(BigDecimal.valueOf(1000.000));
            cartItemRepository.save(cartItem);
        }

        return AddToCartResult.SUCCESS;
    }

    public AddToCartResult deleteItemFromCart(String userId, String productId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        if (cartItem == null) {
            return AddToCartResult.PRODUCT_NOT_FOUND;
        }

        cartItemRepository.delete(cartItem);

        return AddToCartResult.SUCCESS;
    }

    public List<CartItem> getCart(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
