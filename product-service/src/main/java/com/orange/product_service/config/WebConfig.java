package com.orange.product_service.config;

import com.orange.product_service.Interceptor.ProductViewInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ProductViewInterceptor productViewInterceptor;

    public WebConfig(ProductViewInterceptor productViewInterceptor) {
        this.productViewInterceptor = productViewInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(productViewInterceptor)
                .addPathPatterns("/api/products/*"); // apply only on product details
    }
}
