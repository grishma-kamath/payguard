package com.grishma.payguard.gateway.controller;

import com.grishma.payguard.gateway.config.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private final ServiceRegistry serviceRegistry;
    private final WebClient.Builder webClientBuilder;

    public GatewayController(ServiceRegistry serviceRegistry, WebClient.Builder webClientBuilder) {
        this.serviceRegistry = serviceRegistry;
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "API Gateway",
                "version", "3.0.0",
                "timestamp", LocalDateTime.now().toString(),
                "routes", serviceRegistry.getServices()
        ));
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, String>> listServices() {
        return ResponseEntity.ok(serviceRegistry.getServices());
    }

    @GetMapping("/health/{serviceName}")
    public Mono<ResponseEntity<String>> checkServiceHealth(@PathVariable String serviceName) {
        String serviceUrl = serviceRegistry.getServiceUrl(serviceName);
        if (serviceUrl == null) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        log.info("Checking health of service: {} at {}", serviceName, serviceUrl);
        return webClientBuilder.build()
                .get()
                .uri(serviceUrl + "/api/" + resolveHealthPath(serviceName) + "/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().body("Service " + serviceName + " is DOWN"));
    }

    private String resolveHealthPath(String serviceName) {
        return switch (serviceName) {
            case "transaction-service" -> "transactions";
            case "account-service" -> "accounts";
            default -> serviceName;
        };
    }
}
