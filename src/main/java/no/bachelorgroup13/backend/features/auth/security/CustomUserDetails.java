package no.bachelorgroup13.backend.features.auth.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Custom implementation of Spring Security's UserDetails.
 * Stores user authentication and authorization information.
 */
@Data
public class CustomUserDetails implements UserDetails {
    private final UUID id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates a new CustomUserDetails instance.
     * @param id User's unique identifier
     * @param username User's email/username
     * @param password User's encrypted password
     * @param enabled Whether the user account is enabled
     * @param role User's role in the system
     */
    public CustomUserDetails(
            UUID id, String username, String password, boolean enabled, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
