package com.joutio.notification.infrastructure.out.persistence.repository;

import com.joutio.notification.domain.model.NotificationCategory;
import com.joutio.notification.infrastructure.out.persistence.entity.NotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationPreferenceJpaRepository
        extends JpaRepository<NotificationPreferenceEntity, NotificationPreferenceEntity.Key> {

    List<NotificationPreferenceEntity> findByUserId(String userId);

    List<NotificationPreferenceEntity> findByUserIdAndCategory(String userId, NotificationCategory category);
}
