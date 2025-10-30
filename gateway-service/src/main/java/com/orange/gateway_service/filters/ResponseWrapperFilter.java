//package com.orange.gateway_service.filters;
//
//import com.orange.gateway_service.dto.ApiResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.core.Ordered;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Slf4j
//@Component
//public class ResponseWrapperFilter implements GatewayFilter, Ordered {
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//
//        return chain.filter(exchange)
//                .then(Mono.defer(() -> {
//                    // Capture the response
//                    return exchange.getResponse().getBody()
//                            .collectList()
//                            .flatMap(dataBuffers -> {
//                                // Here you could deserialize the body from downstream service
//                                // For simplicity, just wrap whatever you got into ApiResponse
//                                return ApiResponse.success("success", dataBuffers.toString())
//                                        .flatMap(response -> {
//                                            byte[] bytes = response.toString().getBytes();
//                                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
//                                            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
//                                                    .bufferFactory().wrap(bytes)));
//                                        });
//                            });
//                }));
//    }
//
//    @Override
//    public int getOrder() {
//        return -50; // run after JWT filter
//    }
//}
