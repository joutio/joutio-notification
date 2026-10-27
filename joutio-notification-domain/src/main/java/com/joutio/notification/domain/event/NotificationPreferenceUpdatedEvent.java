package com.joutio.notification.domain.event;

import com.joutio.notification.domain.model.NotificationCategory;

import java.time.Instant;

/** Mise à jour d'une préférence de notification (catégorie activée/désactivée). */
public record NotificationPreferenceUpdatedEvent(
        String userId,
        NotificationCategory category,
        boolean enabled,
        Instant updatedAt
) {
}
