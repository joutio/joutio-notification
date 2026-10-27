package com.joutio.notification.infrastructure.out.persistence.mapper;

import com.joutio.notification.domain.model.NotificationPreference;
import com.joutio.notification.infrastructure.out.persistence.entity.NotificationPreferenceEntity;

public final class NotificationPreferenceEntityMapper {

    private NotificationPreferenceEntityMapper() {
    }

    public static NotificationPreference toDomain(NotificationPreferenceEntity entity) {
        return new NotificationPreference(
                entity.getUserId(),
                entity.getCategory(),
                entity.isEnabled(),
                entity.getUpdatedAt());
    }

    public static NotificationPreferenceEntity toEntity(NotificationPreference preference) {
        NotificationPreferenceEntity entity = new NotificationPreferenceEntity();
        entity.setUserId(preference.userId());
        entity.setCategory(preference.category());
        entity.setEnabled(preference.enabled());
        entity.setUpdatedAt(preference.updatedAt());
        return entity;
    }
}
