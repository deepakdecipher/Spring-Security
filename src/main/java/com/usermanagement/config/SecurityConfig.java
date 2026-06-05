package com.usermanagement.config;

import com.usermanagement.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Default security configuration for standalone use or as a starting point.
 *
 * <p><b>To override the security rules</b> in your application, define your own
 * {@code @Bean SecurityFilterChain} — this default bean will back off automatically.
 * Make sure to add the library's {@link JwtAuthenticationFilter} to your chain:
 * <pre>
 *   {@literal @}Bean
 *   public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
 *       http
 *           .csrf(AbstractHttpConfigurer::disable)
 *           .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 *           .authorizeHttpRequests(auth -> auth
 *               .requestMatchers("/userApi/login", "/userApi/sign-up").permitAll()
 *               .anyRequest().authenticated())
 *           .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 *       return http.build();
 *   }
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
