package com.mindbloom.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // =========================
    // PASSWORD ENCODER
    // =========================
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // AUTH PROVIDER (DB AUTH)
    // =========================
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // =========================
    // AUTH MANAGER
    // =========================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // =========================
    // SECURITY RULES
    // =========================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf().disable()

            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/student/forgot-password",
                    "/student/verify-code",
                    "/student/reset-password",
                    "/css/**",
                    "/img/**"
                ).permitAll()

                // Role-based access
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/counselor/**").hasRole("COUNSELOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            // =========================
            // SPRING SECURITY LOGIN
            // =========================
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {

                    String role = authentication
                            .getAuthorities()
                            .iterator()
                            .next()
                            .getAuthority();

                    if (role.equals("ROLE_STUDENT")) {
                        response.sendRedirect(request.getContextPath() + "/student/dashboard");
                    } else if (role.equals("ROLE_COUNSELOR")) {
                        response.sendRedirect(request.getContextPath() + "/counselor/dashboard");
                    } else if (role.equals("ROLE_ADMIN")) {
                        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // =========================
            // LOGOUT
            // =========================
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }
}
