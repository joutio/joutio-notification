package com.joutio.notification.infrastructure.out.messaging.adapter;

import com.joutio.notification.domain.port.out.NotificationEventStorePort;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventStoreAdapter implements NotificationEventStorePort {

    private final EventStore eventStore;

    public NotificationEventStoreAdapter(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public boolean exists(String notificationId) {
        return eventStore.readEvents(notificationId).hasNext();
    }
}
