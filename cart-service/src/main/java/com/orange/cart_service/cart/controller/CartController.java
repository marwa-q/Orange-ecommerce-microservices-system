package com.orange.cart_service.cart.controller;

import com.orange.cart_service.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart Controller", description = "Cart management endpoints")
public class CartController {

}