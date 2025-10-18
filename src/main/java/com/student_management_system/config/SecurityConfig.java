package com.student_management_system.config;

import com.student_management_system.user_management.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

        public SecurityConfig(CustomUserDetailsService userDetailsService,
                        CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
                this.userDetailsService = userDetailsService;
                this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        @SuppressWarnings("deprecation")
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());

                return authProvider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Allow public access to static resources and auth pages
                                                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/login",
                                                                "/register")
                                                .permitAll()
                                                // Role-based access control
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/student/**").hasRole("STUDENT")
                                                .requestMatchers("/teacher/**").hasRole("TEACHER")
                                                .requestMatchers("/parent/**").hasRole("PARENT")
                                                .requestMatchers("/principal/**").hasRole("PRINCIPAL")
                                                .requestMatchers("/staff/**").hasRole("STAFF")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll()
                                );
                return http.build();
        }
}