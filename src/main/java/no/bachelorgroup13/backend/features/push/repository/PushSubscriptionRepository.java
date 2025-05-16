package no.bachelorgroup13.backend.features.push.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import no.bachelorgroup13.backend.features.push.entity.PushNotifications;

/**
 * Repository for managing push notification subscriptions.
 * Provides methods to find and manage user push notification subscriptions.
 */
public interface PushSubscriptionRepository extends CrudRepository<PushNotifications, Long> {
    /**
     * Finds all push notification subscriptions for a specific user.
     * @param userId The ID of the user
     * @return List of push notification subscriptions
     */
    List<PushNotifications> findAllByUserId(UUID userId);

    /**
     * Finds a push notification subscription by its endpoint URL.
     * @param endpoint The endpoint URL of the subscription
     * @return Optional containing the subscription if found
     */
    Optional<PushNotifications> findByEndpoint(String endpoint);
}
