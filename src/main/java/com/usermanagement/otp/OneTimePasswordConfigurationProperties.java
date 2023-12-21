package com.usermanagement.otp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.usermanagement")
public class OneTimePasswordConfigurationProperties {

    @Value("${expiration-minutes}")
    private Integer expirationMinutes;
}