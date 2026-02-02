package com.ems.config;

import com.ems.jwt.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            // H2 console needs CSRF disabled for its path
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            // Allow frames for H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

                // H2 CONSOLE (DEV ONLY)
                .requestMatchers("/h2-console/**").permitAll()

                // PUBLIC
                .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user/login").permitAll()

                // PLANNER
                .requestMatchers(HttpMethod.POST, "/api/planner/event").hasAuthority("PLANNER")
                .requestMatchers(HttpMethod.GET, "/api/planner/events").hasAuthority("PLANNER")
                .requestMatchers(HttpMethod.POST, "/api/planner/resource").hasAuthority("PLANNER")
                .requestMatchers(HttpMethod.GET, "/api/planner/resources").hasAuthority("PLANNER")
                .requestMatchers(HttpMethod.POST, "/api/planner/allocate-resources").hasAuthority("PLANNER")

                // STAFF
                .requestMatchers(HttpMethod.GET, "/api/staff/event-details/**").hasAuthority("STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/staff/update-setup/**").hasAuthority("STAFF")

                // CLIENT
                .requestMatchers(HttpMethod.GET, "/api/client/booking-details/**").hasAuthority("CLIENT")

                // EVERYTHING ELSE
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
