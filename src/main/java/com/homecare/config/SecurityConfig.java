package com.homecare.config;

import com.homecare.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

        configuration.setAllowedOrigins(java.util.List.of(
                "http://localhost:5173",
                "https://homecare-admin-dashboard.vercel.app"
        ));

        configuration.setAllowedMethods(java.util.List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

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

                        // Public
                        .requestMatchers("/api/auth/**", "/api/test").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Authenticated test/debug
                        .requestMatchers("/api/secure-test").authenticated()

                        // Clients
                        .requestMatchers("/api/clients/**")
                        .hasRole("ADMIN")

                        // Caregiver portal / caregiver data
                        .requestMatchers("/api/caregivers/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/client-caregivers/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/caregiver/assignments/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        // Appointments / EVV / clock
                        .requestMatchers("/api/appointments/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/clock/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/visit-notes/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/billing-records/**")
                        .hasAnyRole("ADMIN", "SUPERVISOR", "FINANCE")

                        // Medication / MAR supervisor endpoints - admin only
                        .requestMatchers(
                                "/api/medications/mar/alerts",
                                "/api/medications/mar/review",
                                "/api/medications/mar/compliance-summary",
                                "/api/medications/mar/actions/**"
                        )
                        .hasRole("ADMIN")

                        // Medication pass - caregiver/admin
                        .requestMatchers(
                                "/api/medications/mar/client/*/due",
                                "/api/medications/logs"
                        )
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        // Medication management - admin only
                        .requestMatchers("/api/medications/**")
                        .hasRole("ADMIN")

                        // Documents
                        .requestMatchers("/api/documents/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        // Service documentation submit - caregiver/admin
                        .requestMatchers(HttpMethod.POST, "/api/service-documentation")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        // Service documentation review/history - admin
                        .requestMatchers("/api/service-documentation/**")
                        .hasRole("ADMIN")

                        // Incidents
                        .requestMatchers(HttpMethod.POST, "/api/incidents/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/incidents/**")
                        .hasRole("ADMIN")

                        // Dashboards
                        .requestMatchers("/api/dashboard/admin")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/dashboard/caregiver/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/admin-operations-dashboard/**")
                        .hasRole("ADMIN")

                        // Notifications
                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole("ADMIN", "CAREGIVER", "FAMILY_MEMBER")

                        // ISP - caregiver can read active goals only, admin controls everything else
                        .requestMatchers(HttpMethod.GET, "/api/isp/clients/*/goals/active")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/isp/service-documentation/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/isp/**")
                        .hasRole("ADMIN")

                        // Behavior events
                        .requestMatchers(HttpMethod.POST, "/api/behavior-events")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/behavior-events/options/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/behavior-events/client/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers("/api/behavior-events/service-documentation/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/behavior-events/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/appointment-referrals")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers(HttpMethod.GET, "/api/appointment-referrals/caregiver/**")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                                // Appointment referrals - caregiver/admin specific routes first
                                .requestMatchers(HttpMethod.GET, "/api/appointment-referrals/caregiver/**")
                                .hasAnyRole("ADMIN", "CAREGIVER")

                                .requestMatchers(HttpMethod.POST, "/api/appointment-referrals")
                                .hasAnyRole("ADMIN", "CAREGIVER")

                                .requestMatchers(HttpMethod.GET, "/api/appointment-referrals")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/appointment-referrals/status/**")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/appointment-referrals/client/**")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.PUT, "/api/appointment-referrals/*/review")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.POST, "/api/appointment-referrals/*/convert")
                                .hasRole("ADMIN")

                                .requestMatchers("/api/appointment-referrals/**")
                                .hasRole("ADMIN")

// Appointment reschedule requests - caregiver/admin specific routes first
                                .requestMatchers(HttpMethod.POST, "/api/appointment-reschedule-requests")
                                .hasAnyRole("ADMIN", "CAREGIVER")

                                .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests/caregiver/**")
                                .hasAnyRole("ADMIN", "CAREGIVER")

                                .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests/appointment/**")
                                .hasAnyRole("ADMIN", "CAREGIVER")

                                .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests/client/**")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.PUT, "/api/appointment-reschedule-requests/*/review")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests")
                                .hasRole("ADMIN")

                                .requestMatchers("/api/appointment-reschedule-requests/**")
                                .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/audit-logs")
                        .hasAnyRole("ADMIN", "CAREGIVER")

                        .requestMatchers(HttpMethod.GET, "/api/audit-logs/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/family-portal/**")
                        .hasRole("FAMILY_MEMBER")

                        .requestMatchers(HttpMethod.GET, "/api/documents/*/download")
                        .hasAnyRole("ADMIN", "CAREGIVER", "FAMILY_MEMBER")

                        .requestMatchers("/api/client-family-access/**")
                        .hasRole("ADMIN")



                        // Admin-only modules
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/payroll/**").hasRole("ADMIN")
                        .requestMatchers("/api/client-payroll/**").hasRole("ADMIN")
                        .requestMatchers("/api/invoices/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers("/api/compliance/**").hasRole("ADMIN")
                        .requestMatchers("/api/risk/**").hasRole("ADMIN")
                        .requestMatchers("/api/authorizations/**").hasRole("ADMIN")
                        .requestMatchers("/api/timesheets/**").hasRole("ADMIN")
                        .requestMatchers("/api/evv-alerts/**").hasRole("ADMIN")
                        .requestMatchers("/api/evv-exceptions/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

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