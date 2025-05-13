package no.bachelorgroup13.backend.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.features.auth.security.JwtAuthEntryPoint;
import no.bachelorgroup13.backend.features.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
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
