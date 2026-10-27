package com.joutio.notification.domain.port.out;

import com.joutio.notification.domain.model.NotificationCategory;
import com.joutio.notification.domain.model.NotificationPreference;

import java.util.List;
import java.util.Optional;

/** Port sortant du read model « préférences ». */
public interface NotificationPreferenceRepositoryPort {

    void save(NotificationPreference preference);

    Optional<NotificationPreference> find(String userId, NotificationCategory category);

    List<NotificationPreference> findByUserId(String userId);
}
