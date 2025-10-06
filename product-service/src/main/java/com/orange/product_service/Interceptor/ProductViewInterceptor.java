package com.orange.product_service.Interceptor;

import com.orange.product_service.product.repo.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class ProductViewInterceptor implements HandlerInterceptor {

    private final ProductRepository productRepository;

    public ProductViewInterceptor(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // Only intercept product detail endpoints
        String uri = request.getRequestURI();
        if (uri.matches("/api/products/[0-9a-fA-F-]{8}-[0-9a-fA-F-]{4}-[0-9a-fA-F-]{4}-[0-9a-fA-F-]{4}-[0-9a-fA-F-]{12}")) {  // UUID regex
            String productId = uri.substring(uri.lastIndexOf("/") + 1);

            productRepository.incrementViewCount(UUID.fromString(productId));
        }

        return true; // continue request
    }
}
