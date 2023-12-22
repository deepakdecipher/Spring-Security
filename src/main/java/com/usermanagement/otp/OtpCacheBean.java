package com.usermanagement.otp;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@EnableConfigurationProperties(OneTimePasswordConfigurationProperties.class)
public class OtpCacheBean {

    private final OneTimePasswordConfigurationProperties oneTimePasswordConfigurationProperties;

    @Bean
    public LoadingCache<String, String> loadingCache() {
        final var expirationMinutes = oneTimePasswordConfigurationProperties.getExpirationMinutes();
        return CacheBuilder.newBuilder().expireAfterWrite(expirationMinutes, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    public String load(String key) {
                        return key;
                    }
                });
    }

}