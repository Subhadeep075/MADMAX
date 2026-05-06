package com.digitalcyberseva.backend.config;

import com.digitalcyberseva.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_AUTH_ENDPOINTS = {"/api/auth/**"};
    private static final String[] PUBLIC_SERVICE_ENDPOINTS = {"/api/services/**"};
    private static final String[] PUBLIC_CATEGORY_ENDPOINTS = {"/api/categories/**"};
    private static final String[] PUBLIC_PAYMENT_SETTINGS_ENDPOINTS = {"/api/payment-settings"};
    private static final String[] PUBLIC_CENTER_INFO_ENDPOINTS = {"/api/public/center-info"};
    private static final String[] PUBLIC_UPLOAD_ENDPOINTS = {"/uploads/**"};
    private static final String[] ADMIN_ENDPOINTS = {"/api/admin/**"};
    private static final String[] CUSTOMER_ENDPOINTS = {"/api/requests/**"};
    private static final String[] CUSTOMER_PROFILE_ENDPOINTS = {"/api/customer/profile/**"};
    private static final String[] CUSTOMER_ACCOUNT_DELETION_ENDPOINTS = {"/api/account-deletion/**"};
    private static final String[] CUSTOMER_NOTIFICATION_ENDPOINTS = {"/api/notifications/**"};
    private static final String[] DOCUMENT_DELETE_ENDPOINTS = {"/api/requests/*/documents/*"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_SERVICE_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_CATEGORY_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_PAYMENT_SETTINGS_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_CENTER_INFO_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_UPLOAD_ENDPOINTS).permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, DOCUMENT_DELETE_ENDPOINTS).hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(CUSTOMER_ENDPOINTS).hasRole("CUSTOMER")
                        .requestMatchers(CUSTOMER_PROFILE_ENDPOINTS).hasRole("CUSTOMER")
                        .requestMatchers(CUSTOMER_ACCOUNT_DELETION_ENDPOINTS).hasRole("CUSTOMER")
                        .requestMatchers(CUSTOMER_NOTIFICATION_ENDPOINTS).hasRole("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
