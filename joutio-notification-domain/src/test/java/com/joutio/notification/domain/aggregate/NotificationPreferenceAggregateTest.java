package com.joutio.notification.domain.aggregate;

import com.joutio.axon.test.JoutioAxonMatchers;
import com.joutio.notification.domain.command.NotificationCommand;
import com.joutio.notification.domain.event.NotificationPreferenceUpdatedEvent;
import com.joutio.notification.domain.model.NotificationCategory;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class NotificationPreferenceAggregateTest {

    private static final String USER = "user-1";

    private final AggregateTestFixture<NotificationPreferenceAggregate> fixture =
            new AggregateTestFixture<>(NotificationPreferenceAggregate.class);

    @Test
    void update_createsAggregateIfMissing() {
        fixture.givenNoPriorActivity()
                .when(new NotificationCommand.UpdateNotificationPreferenceCommand(
                        USER, NotificationCategory.ROOM, false))
                .expectEventsMatching(JoutioAxonMatchers.singlePayloadMatching(
                        NotificationPreferenceUpdatedEvent.class,
                        e -> !((NotificationPreferenceUpdatedEvent) e).enabled()
                                && ((NotificationPreferenceUpdatedEvent) e).category()
                                == NotificationCategory.ROOM));
    }

    @Test
    void update_onExistingAggregate_appliesEvent() {
        fixture.given(new NotificationPreferenceUpdatedEvent(
                        USER, NotificationCategory.ROOM, false, java.time.Instant.now()))
                .when(new NotificationCommand.UpdateNotificationPreferenceCommand(
                        USER, NotificationCategory.ROOM, true))
                .expectEventsMatching(JoutioAxonMatchers.hasPayloadMatching(
                        NotificationPreferenceUpdatedEvent.class,
                        e -> ((NotificationPreferenceUpdatedEvent) e).enabled()));
    }
}
