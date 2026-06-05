package com.usermanagement.otp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.usermanagement.config.UserManagementProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configures the Guava {@link LoadingCache} used for OTP storage.
 * Expiry is driven by {@link UserManagementProperties.Otp#getExpirationMinutes()}.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(UserManagementProperties.class)
public class OtpCacheBean {

    private final UserManagementProperties properties;

    @Bean
    public LoadingCache<String, String> otpLoadingCache() {
        int expiryMinutes = properties.getOtp().getExpirationMinutes();
        return CacheBuilder.newBuilder()
                .expireAfterWrite(expiryMinutes, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return key;
                    }
                });
    }
}
