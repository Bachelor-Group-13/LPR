package no.bachelorgroup13.backend.features.push.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.bachelorgroup13.backend.features.user.entity.User;

/**
 * Entity representing a push notification subscription.
 * Stores the necessary information for sending push notifications to users.
 */
@Entity
@Table(name = "push_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushNotifications {

    /**
     * Unique identifier for the push notification subscription.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The endpoint URL for sending push notifications.
     * Must be unique and cannot be null.
     */
    @Column(nullable = false, unique = true, length = 2048)
    private String endpoint;

    /**
     * The public key used for push notification encryption.
     * Cannot be null.
     */
    @Column(nullable = false, length = 512)
    private String p256dh;

    /**
     * The authentication secret for push notifications.
     * Cannot be null.
     */
    @Column(nullable = false, length = 128)
    private String auth;

    /**
     * The ID of the user who owns this subscription.
     * Cannot be null.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * The user entity associated with this subscription.
     * Lazily loaded to improve performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
