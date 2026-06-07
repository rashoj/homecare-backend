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

    private static final String[] PLATFORM_ROLES = {
            "PLATFORM_OWNER",
            "PLATFORM_ADMIN"
    };

    private static final String[] AGENCY_ADMIN_ROLES = {
            "AGENCY_ADMIN",
            "SCHEDULER",
            "SUPERVISOR",
            "FINANCE",
            "ADMIN"
    };

    private static final String[] AGENCY_CARE_ROLES = {
            "AGENCY_ADMIN",
            "SCHEDULER",
            "SUPERVISOR",
            "ADMIN",
            "CAREGIVER"
    };

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
                "https://homecare-admin-dashboard.vercel.app",
                "https://homecare-admin-dashboard-git-main-rashojontas-projects.vercel.app"
        ));

        configuration.setAllowedMethods(java.util.List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setExposedHeaders(java.util.List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth

                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public
                        .requestMatchers("/api/auth/**", "/api/test").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Platform owner/admin only
                        .requestMatchers("/api/platform/**")
                        .hasAnyRole(PLATFORM_ROLES)

                        // Authenticated test/debug
                        .requestMatchers("/api/secure-test").authenticated()

                        // Clients
                        .requestMatchers("/api/clients/**")
                        .hasAnyRole(AGENCY_ADMIN_ROLES)

                        // Caregiver portal / caregiver data
                        .requestMatchers("/api/caregivers/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER")

                        .requestMatchers("/api/client-caregivers/**")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/caregiver/assignments/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        // Appointments / EVV / clock
                        .requestMatchers("/api/appointments/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/clock/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/visit-notes/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/billing-records/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "FINANCE", "ADMIN")

                        // Medication / MAR supervisor endpoints
                        .requestMatchers(
                                "/api/medications/mar/alerts",
                                "/api/medications/mar/review",
                                "/api/medications/mar/compliance-summary",
                                "/api/medications/mar/actions/**"
                        )
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Medication pass - caregiver/admin
                        .requestMatchers(
                                "/api/medications/mar/client/*/due",
                                "/api/medications/logs"
                        )
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER")

                        // Medication management
                        .requestMatchers("/api/medications/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Documents
                        .requestMatchers("/api/documents/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER", "FAMILY_MEMBER")

                        // Service documentation submit
                        .requestMatchers(HttpMethod.POST, "/api/service-documentation")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER")

                        // Service documentation review/history
                        .requestMatchers("/api/service-documentation/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Incidents
                        .requestMatchers(HttpMethod.POST, "/api/incidents/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER")

                        .requestMatchers("/api/incidents/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Dashboards
                        .requestMatchers("/api/dashboard/admin")
                        .hasAnyRole(AGENCY_ADMIN_ROLES)

                        .requestMatchers("/api/dashboard/admin/**")
                        .hasAnyRole(AGENCY_ADMIN_ROLES)

                        .requestMatchers("/api/dashboard/caregiver/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/admin-operations-dashboard/**")
                        .hasAnyRole(AGENCY_ADMIN_ROLES)

                        // Notifications
                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER", "FAMILY_MEMBER")

                        // ISP
                        .requestMatchers(HttpMethod.GET, "/api/isp/clients/*/goals/active")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/isp/service-documentation/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/isp/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Behavior events
                        .requestMatchers(HttpMethod.POST, "/api/behavior-events")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/behavior-events/options/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/behavior-events/client/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers("/api/behavior-events/service-documentation/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/behavior-events/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Appointment referrals
                        .requestMatchers(HttpMethod.GET, "/api/appointment-referrals/caregiver/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers(HttpMethod.POST, "/api/appointment-referrals")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/appointment-referrals")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/appointment-referrals/status/**")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/appointment-referrals/client/**")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/appointment-referrals/*/review")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/appointment-referrals/*/convert")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/appointment-referrals/**")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        // Appointment reschedule requests
                        .requestMatchers(HttpMethod.POST, "/api/appointment-reschedule-requests")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests/caregiver/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests/appointment/**")
                        .hasAnyRole(AGENCY_CARE_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests/client/**")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/appointment-reschedule-requests/*/review")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/appointment-reschedule-requests")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/appointment-reschedule-requests/**")
                        .hasAnyRole("AGENCY_ADMIN", "SCHEDULER", "SUPERVISOR", "ADMIN")

                        // Audit logs
                        .requestMatchers(HttpMethod.POST, "/api/audit-logs")
                        .hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER")

                        .requestMatchers(HttpMethod.GET, "/api/audit-logs/**")
                        .hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Family portal
                        .requestMatchers("/api/family-portal/**")
                        .hasRole("FAMILY_MEMBER")

                        .requestMatchers(HttpMethod.GET, "/api/documents/*/download")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN", "CAREGIVER", "FAMILY_MEMBER")

                        .requestMatchers("/api/client-family-access/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        // Agency admin modules
                        .requestMatchers("/api/users/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/payroll/**")
                        .hasAnyRole("AGENCY_ADMIN", "FINANCE", "ADMIN")

                        .requestMatchers("/api/client-payroll/**")
                        .hasAnyRole("AGENCY_ADMIN", "FINANCE", "ADMIN")

                        .requestMatchers("/api/invoices/**")
                        .hasAnyRole("AGENCY_ADMIN", "FINANCE", "ADMIN")

                        .requestMatchers("/api/reports/**")
                        .hasAnyRole(AGENCY_ADMIN_ROLES)

                        .requestMatchers("/api/compliance/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/risk/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/authorizations/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "FINANCE", "ADMIN")

                        .requestMatchers("/api/timesheets/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "FINANCE", "ADMIN")

                        .requestMatchers("/api/evv-alerts/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

                        .requestMatchers("/api/evv-exceptions/**")
                        .hasAnyRole("AGENCY_ADMIN", "SUPERVISOR", "ADMIN")

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