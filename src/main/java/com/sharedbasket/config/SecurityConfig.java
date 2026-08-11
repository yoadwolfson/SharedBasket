package com.sharedbasket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/api/auth/register"),
                    new AntPathRequestMatcher("/api/auth/login"),
                    new AntPathRequestMatcher("/javax.faces.resource/**"),
                    new AntPathRequestMatcher("/resources/**"),
                    new AntPathRequestMatcher("/webjars/**"),
                    new AntPathRequestMatcher("/h2-console/**"),
                    new AntPathRequestMatcher("/**/*.xhtml"),
                    new AntPathRequestMatcher("/**/*.jsf"),
                    new AntPathRequestMatcher("/")
                ).permitAll()
                .requestMatchers(
                    new AntPathRequestMatcher("/api/auth/me"),
                    new AntPathRequestMatcher("/api/auth/logout"),
                    new AntPathRequestMatcher("/api/profile/**")
                ).authenticated()
                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.xhtml")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .formLogin(form -> form.disable());

        return http.build();
    }
}
