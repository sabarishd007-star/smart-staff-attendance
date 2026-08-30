package com.smartattendance.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          RateLimitingFilter rateLimitingFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) // Permit H2 console frames
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com https://cdn.tailwindcss.com https://unpkg.com/@babel/standalone https://unpkg.com/react@18/umd/react.production.min.js https://unpkg.com/react-dom@18/umd/react-dom.production.min.js; " +
                        "style-src 'self' 'unsafe-inline' https://unpkg.com;"))
                .xssProtection(xss -> xss.disable()) // Rely on CSP instead of legacy X-XSS-Protection
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**").permitAll()
                .requestMatchers("/api/v1/auth/**", "/v1/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Permit H2 console access
                .requestMatchers("/api/v1/admin/**", "/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/hod/**", "/v1/hod/**").hasAnyRole("HOD", "ADMIN")
                .requestMatchers("/api/v1/attendance/**", "/v1/attendance/**").hasAnyRole("STAFF", "HOD", "ADMIN")
                .requestMatchers("/api/v1/leave/**", "/v1/leave/**").hasAnyRole("STAFF", "HOD", "ADMIN")
                .requestMatchers("/api/v1/staff/**", "/v1/staff/**", "/api/v1/notifications/**", "/v1/notifications/**").authenticated()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            // Order: RateLimitingFilter -> JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        // Explicit allowlist supporting wildcards for Vercel preview/branch builds
        configuration.setAllowedOriginPatterns(java.util.List.of(
            "https://smart-staff-attendance-zysd.vercel.app",
            "https://smart-staff-attendance-*.vercel.app",
            "https://*.vercel.app",
            "http://localhost:5173",
            "http://localhost:3000"
        ));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"));
        configuration.setExposedHeaders(java.util.List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache pre-flight response for 1 hour
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
