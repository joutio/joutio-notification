package com.joutio.notification.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationTypeTest {

    @Test
    void aChallengeFromAGameIsARematch() {
        assertEquals(NotificationCategory.REMATCH, NotificationType.CHALLENGE_RECEIVED.category("game-1"));
        assertEquals(NotificationCategory.ROOM, NotificationType.CHALLENGE_RECEIVED.category(null));
    }

    @Test
    void otherTypesKeepTheirCategory() {
        assertEquals(NotificationCategory.FOLLOW, NotificationType.FOLLOW.category(null));
        assertEquals(NotificationCategory.QUESTION, NotificationType.QUESTION_REPORTED.category(null));
        assertEquals(NotificationCategory.ROOM, NotificationType.ROOM_ACCEPTED.category("game-1"));
        assertEquals(NotificationCategory.ACCOUNT, NotificationType.ACCOUNT_SUSPENDED.category(null));
    }
}
