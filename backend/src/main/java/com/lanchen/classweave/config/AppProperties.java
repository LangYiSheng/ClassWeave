package com.lanchen.classweave.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Security security,
        Share share
) {

    public record Security(Jwt jwt) {
    }

    public record Jwt(String secret, long expirationHours) {
    }

    public record Share(String baseUrl) {
    }
}
