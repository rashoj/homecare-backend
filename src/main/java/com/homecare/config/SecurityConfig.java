package com.homecare.config;

import com.homecare.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration =
                new org.springframework.web.cors.CorsConfiguration();

        configuration.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**", "/api/test").permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                     //   .requestMatchers("/api/debug/me").authenticated()
                        .requestMatchers("/api/secure-test").authenticated()

                        .requestMatchers("/api/clients/**").hasRole("ADMIN")

                        .requestMatchers("/api/caregivers/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/client-caregivers/**").hasRole("ADMIN")

                        .requestMatchers("/api/caregiver/assignments/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/appointments/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/clock/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/visit-notes/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/medications/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/documents/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/service-documentation/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/incidents/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")

                        .requestMatchers("/api/dashboard/caregiver/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole("ADMIN", "CAREGIVER", "FAMILY_MEMBER")

                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/payroll/**").hasRole("ADMIN")
                        .requestMatchers("/api/client-payroll/**").hasRole("ADMIN")
                        .requestMatchers("/api/invoices/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers("/api/compliance/**").hasRole("ADMIN")
                        .requestMatchers("/api/risk/**").hasRole("ADMIN")
                        .requestMatchers("/api/authorizations/**").hasRole("ADMIN")
                        .requestMatchers("/api/timesheets/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}