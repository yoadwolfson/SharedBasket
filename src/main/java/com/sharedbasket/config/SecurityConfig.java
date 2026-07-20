package com.sharedbasket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Shared Basket Application
 * 
 * Temporarily configured for development to allow viewing JSF pages
 * without authentication. This should be updated when Developer A
 * provides the actual authentication service.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/resources/**", "/webjars/**", "/h2-console/**").permitAll()
                .antMatchers("/**/*.xhtml", "/**/*.jsf", "/").permitAll()
                .anyRequest().permitAll()
            .and()
                .csrf().disable()
                .headers().frameOptions().disable();
        
        return http.build();
    }
}
