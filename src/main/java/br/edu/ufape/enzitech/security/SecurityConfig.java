package br.edu.ufape.enzitech.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Acesso Negado.");
                })
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/users").permitAll()
                    .requestMatchers(HttpMethod.GET, "/users").permitAll()
                    .requestMatchers(HttpMethod.GET, "/users/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/users/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/users/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/experiments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/experiments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/experiments/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/experiments/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/experiments/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/treatments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/treatments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/treatments/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/treatments/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/treatments/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/enzymes").permitAll()
                    .requestMatchers(HttpMethod.GET, "/enzymes").permitAll()
                    .requestMatchers(HttpMethod.GET, "/enzymes/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/enzymes/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/enzymes/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/experiments/results").permitAll()
                    .requestMatchers(HttpMethod.GET, "/experiments/results").permitAll()
                    .requestMatchers(HttpMethod.GET, "/experiments/results/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/experiments/results/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/experiments/results/**").permitAll()
                    .requestMatchers("/v3/api-docs").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}