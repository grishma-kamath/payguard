package com.grishma.payguard.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "gateway")
public class ServiceRegistry {

    private Map<String, String> services = new HashMap<>();

    public Map<String, String> getServices() { return services; }
    public void setServices(Map<String, String> services) { this.services = services; }

    public String getServiceUrl(String serviceName) {
        return services.get(serviceName);
    }
}
