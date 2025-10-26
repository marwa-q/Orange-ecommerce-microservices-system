package com.orange.gateway_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SwaggerController {

    @GetMapping("/swagger/{service}/index.html")
    public String getSwaggerUI(@PathVariable String service) {
        return "swagger-ui";
    }
}
