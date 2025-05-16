package no.bachelorgroup13.backend.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.features.auth.security.JwtAuthEntryPoint;
import no.bachelorgroup13.backend.features.auth.security.JwtAuthenticationFilter;

/**
 * Security configuration for the application.
 * Handles authentication, authorization, and CORS settings.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the authentication provider with user details service and password encoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Creates and configures the authentication manager.
     * @param authConfig Authentication configuration
     * @return Configured authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Creates a BCrypt password encoder with strength of 10.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Configures the security filter chain with CORS, CSRF, and endpoint authorization rules.
     * @param http HttpSecurity configuration
     * @return Configured security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(
                        exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/auth/signin",
                                                "/api/auth/signup",
                                                "/api/auth/refresh")
                                        .permitAll()
                                        .requestMatchers("/license-plate")
                                        .permitAll()
                                        .requestMatchers("/api/reservations/**")
                                        .permitAll()
                                        .requestMatchers("/api/push/publicKey")
                                        .permitAll()
                                        .requestMatchers("/api/push/subscribe")
                                        .authenticated()
                                        .requestMatchers("/api/push/**")
                                        .permitAll()
                                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                        .permitAll()
                                        .requestMatchers("/license-plate/**")
                                        .permitAll()
                                        .requestMatchers("/api/auth/license-plate/**")
                                        .permitAll()
                                        .requestMatchers("/api/dev/**")
                                        .hasRole("DEVELOPER")
                                        .requestMatchers("/api/user/**")
                                        .hasAnyRole("USER", "DEVELOPER")
                                        .anyRequest()
                                        .authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS settings for the application.
     * Allows specific origins and sets up allowed methods and headers.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                List.of("https://129.241.152.242.nip.io", "http://localhost:3000"));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        System.out.println("CORS Configuration:");
        System.out.println("Allowed Origins: " + configuration.getAllowedOrigins());
        System.out.println("Allowed Methods: " + configuration.getAllowedMethods());
        System.out.println("Allowed Headers: " + configuration.getAllowedHeaders());
        System.out.println("Allow Credentials: " + configuration.getAllowCredentials());
        return source;
    }
}
