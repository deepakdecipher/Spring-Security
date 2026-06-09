package com.usermanagement.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.usermanagement.config.UserManagementProperties;
import com.usermanagement.emailservice.EmailService;
import com.usermanagement.emailservice.emailserviceimpl.EmailServiceImpl;
import com.usermanagement.brevo.BrevoEmailService;
import com.usermanagement.exceptionhandler.CustomAccessDeniedHandler;
import com.usermanagement.exceptionhandler.CustomExceptionHandler;
import com.usermanagement.jwt.JwtAuthenticationFilter;
import com.usermanagement.modelentity.User;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.CustomUserDetailsService;

/**
 * Spring Boot Auto-Configuration entry point for the user-management library.
 *
 * <p>This class is picked up automatically via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * whenever the library jar is on the classpath.
 *
 * <h3>What it activates</h3>
 * <ul>
 *   <li>All {@code @Service}, {@code @Repository}, {@code @Component} beans in
 *       {@code com.usermanagement.*} packages (via @ComponentScan)</li>
 *   <li>{@link UserManagementProperties} configuration properties</li>
 *   <li>Scheduled OTP cache cleanup ({@code @EnableScheduling})</li>
 * </ul>
 *
 * <h3>Overriding library defaults</h3>
 * Every overridable bean is annotated with
 * {@code @ConditionalOnMissingBean}. Define a bean of the same type in your
 * application and the library's default will back off automatically.
 *
 * <p>Key override points:
 * <table>
 *   <tr><th>Bean type</th><th>Purpose</th></tr>
 *   <tr><td>{@code SecurityFilterChain}</td><td>Route security rules</td></tr>
 *   <tr><td>{@code UserDetailsService}</td><td>Load users (custom entity)</td></tr>
 *   <tr><td>{@code PasswordEncoder}</td><td>Hashing algorithm</td></tr>
 *   <tr><td>{@code EmailService}</td><td>OTP email delivery</td></tr>
 *   <tr><td>{@code UserRestController}</td><td>User API endpoints</td></tr>
 *   <tr><td>{@code RoleRestController}</td><td>Role API endpoints</td></tr>
 * </table>
 */
@AutoConfiguration(beforeName = "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration")
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@EnableConfigurationProperties(UserManagementProperties.class)
@ComponentScan(basePackages = "com.usermanagement")
@EntityScan(basePackageClasses = User.class)
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
public class UserManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public CustomUserDetailsService customUserDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService emailService(ObjectProvider<JavaMailSender> mailSender,
                                     UserManagementProperties properties) {
        if ("brevo".equalsIgnoreCase(properties.getEmail().getProvider())) {
            return new BrevoEmailService(properties);
        }
        return new EmailServiceImpl(mailSender, properties);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                        .authenticationEntryPoint(new CustomExceptionHandler()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/userApi/login",
                                "/userApi/login-otp",
                                "/userApi/sign-up",
                                "/userApi/verify-otp/**",
                                "/userApi/generate-otp/**",
                                "/userApi/reset-password"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
