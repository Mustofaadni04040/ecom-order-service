package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.model.AddToCartResult;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart (
        @RequestHeader("X-User-ID") String userId,
        @RequestBody CartItemRequest request
    ) {
        AddToCartResult result = cartService.addToCart(userId, request);

        return switch (result) {
            case PRODUCT_NOT_FOUND -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Product not found");
            case OUT_OF_STOCK -> ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Product out of stock");
            case USER_NOT_FOUND -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
            case SUCCESS -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Product added to cart");
            default -> ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error");
        };
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeFromCart(@RequestHeader("X-User-id") String userId, @PathVariable String productId) {
        AddToCartResult removed = cartService.deleteItemFromCart(userId, productId);

        return switch (removed) {
            case PRODUCT_NOT_FOUND -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Product not found");
            case OUT_OF_STOCK -> ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Product out of stock");
            case USER_NOT_FOUND -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
            case SUCCESS -> ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Product deleted to cart");
            default -> ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error");
        };
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(@RequestHeader("X-User-id") String userId) {
        return new ResponseEntity<>(cartService.getCart(userId), HttpStatus.OK);
    }
}
