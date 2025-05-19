package no.bachelorgroup13.backend.features.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter for JWT authentication.
 * Validates JWT tokens from cookies and sets up a security context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    /**
     * Processes each request to validate a JWT token and set up authentication.
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Optional<String> jwt = getJwtFromCookie(request);
            log.info("JWT from cookie: {}", jwt.orElse("not found"));

            if (jwt.isPresent() && tokenProvider.validateToken(jwt.get())) {
                Authentication authentication = tokenProvider.getAuthentication(jwt.get());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Successfully authenticated user");
            } else {
                log.info("No valid JWT token found in cookie");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from request cookies.
     * @param request HTTP request containing cookies
     * @return Optional containing JWT token if found
     */
    private Optional<String> getJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "user".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue);
        }
        return Optional.empty();
    }
}
