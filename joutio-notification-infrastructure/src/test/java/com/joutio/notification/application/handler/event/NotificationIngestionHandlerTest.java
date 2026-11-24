package com.joutio.notification.application.handler.event;

import com.joutio.identity.domain.event.UserEvent;
import com.joutio.matchmaking.domain.event.RoomEvent;
import com.joutio.matchmaking.domain.model.RoomCancelReason;
import com.joutio.matchmaking.domain.model.RoomOrigin;
import com.joutio.matchmaking.domain.model.RoomReminderStage;
import com.joutio.matchmaking.domain.model.RoomStatus;
import com.joutio.notification.domain.command.NotificationCommand;
import com.joutio.notification.domain.model.Notification;
import com.joutio.notification.domain.model.NotificationCategory;
import com.joutio.notification.domain.model.NotificationPreference;
import com.joutio.notification.domain.model.NotificationType;
import com.joutio.notification.domain.port.out.NotificationPreferenceRepositoryPort;
import com.joutio.notification.domain.port.out.NotificationRepositoryPort;
import com.joutio.social.domain.event.UserFollowerEvent;
import com.joutio.theme.domain.event.QuestionReportEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import com.joutio.notification.domain.port.out.NotificationEventStorePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Ingestion des événements de domaine : invitations et relances des salles (inbox, {@code sourceId = roomId}),
 * le reste de la salle restant hors inbox ({@code ROOM_COMPLETED} rattache le {@code gameId} à l'acceptation).
 *
 * <p>La création est idempotente même après un **hard delete** : une relecture Kafka
 * at-least-once ne doit pas tenter de recréer un agrégat existant (poison pill).</p>
 */
class NotificationIngestionHandlerTest {

    private static final Instant AT = Instant.parse("2026-10-03T18:02:00Z");

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final NotificationRepositoryPort notificationRepository = mock(NotificationRepositoryPort.class);
    private final NotificationPreferenceRepositoryPort preferenceRepository = mock(NotificationPreferenceRepositoryPort.class);
    private final NotificationEventStorePort eventStore = mock(NotificationEventStorePort.class);
    private final NotificationIngestionHandler handler = new NotificationIngestionHandler(
            commandGateway, notificationRepository, preferenceRepository, eventStore);

    @BeforeEach
    void setUp() {
        when(eventStore.exists(anyString())).thenReturn(false);
    }

    @Test
    void accountSuspended_isMandatoryEvenWhenAccountPreferenceDisabled() {
        when(preferenceRepository.find("user-1", NotificationCategory.ACCOUNT))
                .thenReturn(Optional.of(new NotificationPreference(
                        "user-1", NotificationCategory.ACCOUNT, false, AT)));

        handler.on(new UserEvent.UserSuspendedEvent(
                "user-1", "user@joutio.dev", "suspension-1",
                com.joutio.identity.domain.model.UserSuspensionReason.HARASSMENT,
                "internal", "admin-1", AT, null));

        ArgumentCaptor<NotificationCommand.CreateNotificationCommand> captor =
                ArgumentCaptor.forClass(NotificationCommand.CreateNotificationCommand.class);
        verify(commandGateway).send(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo("user-1");
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.ACCOUNT_SUSPENDED);
        assertThat(captor.getValue().sourceId()).isEqualTo("suspension-1");
        assertThat(captor.getValue().actorId()).isNull();
    }

    @Test
    void questionReported_notifiesTheTopicCreatorWithoutRevealingTheReporter() {
        handler.on(new QuestionReportEvent.QuestionReportedEvent(
                "thread-1", "report-1", "question-1", "topic-1", "creator-1",
                "reporter-1", "game-1", java.util.Set.of(
                        com.joutio.theme.domain.model.QuestionReportReason.ANSWERS),
                "Bonne réponse fausse", null, null, AT));

        ArgumentCaptor<NotificationCommand.CreateNotificationCommand> captor =
                ArgumentCaptor.forClass(NotificationCommand.CreateNotificationCommand.class);
        verify(commandGateway).send(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo("creator-1");
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.QUESTION_REPORTED);
        assertThat(captor.getValue().actorId()).isNull();
        assertThat(captor.getValue().sourceId()).isEqualTo("report-1");
        assertThat(captor.getValue().topicId()).isEqualTo("topic-1");
    }

    @Test
    void questionReported_withoutTopicCreator_doesNothing() {
        handler.on(new QuestionReportEvent.QuestionReportedEvent(
                "thread-1", "report-1", "question-1", "topic-1", null,
                "reporter-1", "game-1", java.util.Set.of(
                        com.joutio.theme.domain.model.QuestionReportReason.ANSWERS),
                null, null, null, AT));

        verifyNoInteractions(commandGateway);
    }

    @Test
    void challengeSent_notifiesTheInviteeWithTheRoomAsSource() {
        handler.on(invited("room-1", RoomOrigin.CHALLENGE, null, AT.plusSeconds(3600)));

        NotificationCommand.CreateNotificationCommand created = createdNotification();
        assertThat(created.userId()).isEqualTo("opponent");
        assertThat(created.type()).isEqualTo(NotificationType.CHALLENGE_RECEIVED);
        assertThat(created.actorId()).isEqualTo("challenger");
        assertThat(created.sourceId()).isEqualTo("room-1");
        assertThat(created.gameId()).isNull();
        assertThat(created.expiresAt()).isEqualTo(AT.plusSeconds(3600));
    }

    @Test
    void rematchSent_carriesTheSourceGame() {
        handler.on(invited("room-1", RoomOrigin.REMATCH, "game-1", AT.plusSeconds(10)));

        NotificationCommand.CreateNotificationCommand created = createdNotification();
        assertThat(created.gameId()).isEqualTo("game-1");
        assertThat(created.expiresAt()).isEqualTo(AT.plusSeconds(10));
    }

    /** Revanche coupée par le joueur : rien n'est créé, alors qu'un défi ordinaire (ROOM) passe toujours. */
    @Test
    void rematch_respectsTheRematchPreferenceOnly() {
        when(preferenceRepository.find("opponent", NotificationCategory.REMATCH))
                .thenReturn(Optional.of(new NotificationPreference("opponent", NotificationCategory.REMATCH, false, AT)));

        handler.on(invited("room-1", RoomOrigin.REMATCH, "game-1", AT.plusSeconds(10)));
        verifyNoInteractions(commandGateway);

        handler.on(invited("room-2", RoomOrigin.CHALLENGE, null, AT.plusSeconds(3600)));
        verify(commandGateway).send(any(NotificationCommand.CreateNotificationCommand.class));
    }

    @Test
    void sharedOrMatchmakingRoom_doesNotNotify() {
        handler.on(new RoomEvent.RoomCreatedEvent(
                "room-1", "topic-1", "initiator", null, false, null, RoomOrigin.SHARED, null,
                RoomStatus.WAITING_PLAYERS, AT.plusSeconds(86_400), AT));
        handler.on(new RoomEvent.RoomCreatedEvent(
                "room-2", "topic-1", "initiator", "opponent", false, null, RoomOrigin.MATCHMAKING, null,
                RoomStatus.WAITING_PLAYERS, AT.plusSeconds(86_400), AT));

        verifyNoInteractions(commandGateway);
    }

    @Test
    void accepted_expiresTheInvitationAndNotifiesTheChallenger() {
        handler.on(new RoomEvent.RoomAcceptedEvent(
                "room-1", "topic-1", "challenger", "opponent", RoomOrigin.CHALLENGE, AT.plusSeconds(86_400), AT));

        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.CHALLENGE_RECEIVED, AT);
        NotificationCommand.CreateNotificationCommand created = createdNotification();
        assertThat(created.userId()).isEqualTo("challenger");
        assertThat(created.type()).isEqualTo(NotificationType.ROOM_ACCEPTED);
        assertThat(created.actorId()).isEqualTo("opponent");
        assertThat(created.sourceId()).isEqualTo("room-1");
        assertThat(created.topicId()).isEqualTo("topic-1");
    }

    @Test
    void declined_expiresTheInvitationAndNotifiesTheChallenger() {
        handler.on(new RoomEvent.RoomDeclinedEvent(
                "room-1", "topic-1", "challenger", "opponent", RoomOrigin.CHALLENGE, AT));

        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.CHALLENGE_RECEIVED, AT);
        NotificationCommand.CreateNotificationCommand created = createdNotification();
        assertThat(created.userId()).isEqualTo("challenger");
        assertThat(created.type()).isEqualTo(NotificationType.CHALLENGE_DECLINED);
        assertThat(created.actorId()).isEqualTo("opponent");
        assertThat(created.sourceId()).isEqualTo("room-1");
    }

    @Test
    void cancelled_deletesTheInvitationAndExpiresThePendingWizz() {
        when(notificationRepository.findBySourceIdAndType("room-1", NotificationType.CHALLENGE_RECEIVED))
                .thenReturn(List.of(notification("notification-1", NotificationType.CHALLENGE_RECEIVED, AT)));

        handler.on(new RoomEvent.RoomCancelledEvent(
                "room-1", "challenger", RoomCancelReason.PLAYER_CANCELLED, Set.of("challenger", "opponent"), AT));

        ArgumentCaptor<NotificationCommand.DeleteNotificationCommand> captor =
                ArgumentCaptor.forClass(NotificationCommand.DeleteNotificationCommand.class);
        verify(commandGateway).sendAndWait(captor.capture());
        assertThat(captor.getValue().notificationId()).isEqualTo("notification-1");
        assertThat(captor.getValue().userId()).isEqualTo("opponent");
        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.WIZZ, AT);
    }

    @Test
    void cancelled_withoutInvitation_sendsNoCommand() {
        when(notificationRepository.findBySourceIdAndType("room-1", NotificationType.CHALLENGE_RECEIVED))
                .thenReturn(List.of());

        handler.on(new RoomEvent.RoomCancelledEvent(
                "room-1", "initiator", RoomCancelReason.PLAYER_CANCELLED, Set.of("initiator"), AT));

        verifyNoInteractions(commandGateway);
    }

    @Test
    void invitationReminder_replacesTheInvitationWithAFreshUnreadOne() {
        when(notificationRepository.findBySourceIdAndType("room-1", NotificationType.CHALLENGE_RECEIVED))
                .thenReturn(List.of(notification("notification-1", NotificationType.CHALLENGE_RECEIVED, AT.minusSeconds(900))));

        handler.on(reminded(RoomReminderStage.INVITATION, AT));

        ArgumentCaptor<NotificationCommand.DeleteNotificationCommand> deleteCaptor =
                ArgumentCaptor.forClass(NotificationCommand.DeleteNotificationCommand.class);
        verify(commandGateway).sendAndWait(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue().notificationId()).isEqualTo("notification-1");

        NotificationCommand.CreateNotificationCommand created = createdNotification();
        assertThat(created.userId()).isEqualTo("opponent");
        assertThat(created.type()).isEqualTo(NotificationType.CHALLENGE_RECEIVED);
        assertThat(created.sourceId()).isEqualTo("room-1");
        assertThat(created.topicId()).isEqualTo("topic-1");
        assertThat(created.expiresAt()).isEqualTo(AT.plusSeconds(2700));
        assertThat(created.notificationId()).isNotEqualTo("notification-1");
    }

    @Test
    void presenceReminder_replacesThePreviousWizzAndNotifiesTheTarget() {
        when(notificationRepository.findBySourceIdAndType("room-1", NotificationType.WIZZ))
                .thenReturn(List.of(notification("notification-previous", NotificationType.WIZZ, AT.minusSeconds(20))));

        handler.on(reminded(RoomReminderStage.PRESENCE, AT));

        ArgumentCaptor<NotificationCommand.DeleteNotificationCommand> deleteCaptor =
                ArgumentCaptor.forClass(NotificationCommand.DeleteNotificationCommand.class);
        verify(commandGateway).sendAndWait(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue().notificationId()).isEqualTo("notification-previous");

        NotificationCommand.CreateNotificationCommand created = createdNotification();
        assertThat(created.userId()).isEqualTo("opponent");
        assertThat(created.type()).isEqualTo(NotificationType.WIZZ);
        assertThat(created.actorId()).isEqualTo("challenger");
        assertThat(created.sourceId()).isEqualTo("room-1");
        assertThat(created.topicId()).isNull();
        assertThat(created.expiresAt()).isNull();
    }

    @Test
    void reminder_secondDeliveryReusesTheSameDeterministicId() {
        handler.on(reminded(RoomReminderStage.PRESENCE, AT));
        handler.on(reminded(RoomReminderStage.PRESENCE, AT));

        ArgumentCaptor<NotificationCommand.CreateNotificationCommand> captor =
                ArgumentCaptor.forClass(NotificationCommand.CreateNotificationCommand.class);
        verify(commandGateway, times(2)).send(captor.capture());
        assertThat(captor.getAllValues().get(0).notificationId())
                .isEqualTo(captor.getAllValues().get(1).notificationId());
    }

    @Test
    void expired_expiresTheInvitationAndTheWizzWithoutNotification() {
        handler.on(new RoomEvent.RoomExpiredEvent("room-1", Set.of("challenger", "opponent"), AT));

        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.CHALLENGE_RECEIVED, AT);
        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.WIZZ, AT);
        verifyNoInteractions(commandGateway);
    }

    @Test
    void purged_expiresWhatIsStillActionable() {
        handler.on(new RoomEvent.RoomPurgedEvent("room-1", AT));

        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.CHALLENGE_RECEIVED, AT);
        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.WIZZ, AT);
        verifyNoInteractions(commandGateway);
    }

    @Test
    void completed_attachesTheGameToTheAcceptance() {
        handler.on(new RoomEvent.RoomCompletedEvent(
                "room-1", "game-1", RoomOrigin.CHALLENGE, "challenger", Set.of("challenger", "opponent"), AT));

        verify(notificationRepository).attachGameId("room-1", "game-1");
        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.WIZZ, AT);
        verifyNoInteractions(commandGateway);
    }

    @Test
    void roomEntered_expiresTheWizzOfTheArrivingPlayer() {
        handler.on(new RoomEvent.RoomEnteredEvent("room-1", "opponent", null, AT, "presence-1", Set.of("opponent")));

        verify(notificationRepository).expireBySourceIdAndTypeAndUserId(
                "room-1", NotificationType.WIZZ, "opponent", AT);
    }

    @Test
    void roomFailed_expiresPendingWizz() {
        handler.on(new RoomEvent.RoomFailedEvent("room-1", "GAME_CREATION_TIMEOUT", AT));

        verify(notificationRepository).expireBySourceIdAndType("room-1", NotificationType.WIZZ, AT);
    }

    private static RoomEvent.RoomCreatedEvent invited(String roomId, RoomOrigin origin, String sourceGameId, Instant expiresAt) {
        return new RoomEvent.RoomCreatedEvent(
                roomId, "topic-1", "challenger", "opponent", false, null, origin, sourceGameId,
                RoomStatus.INVITED, expiresAt, AT);
    }

    private static RoomEvent.RoomRemindedEvent reminded(RoomReminderStage stage, Instant at) {
        return new RoomEvent.RoomRemindedEvent(
                "room-1", "challenger", "opponent", stage, "topic-1", null, AT.plusSeconds(2700), at, at.plusSeconds(600));
    }

    private static Notification notification(String id, NotificationType type, Instant createdAt) {
        return Notification.builder()
                .notificationId(id)
                .userId("opponent")
                .type(type)
                .actorId("challenger")
                .sourceId("room-1")
                .createdAt(createdAt)
                .build();
    }

    private NotificationCommand.CreateNotificationCommand createdNotification() {
        ArgumentCaptor<NotificationCommand.CreateNotificationCommand> captor =
                ArgumentCaptor.forClass(NotificationCommand.CreateNotificationCommand.class);
        verify(commandGateway).send(captor.capture());
        return captor.getValue();
    }

    @Test
    void followReplayedAfterDelete_isSkippedWhenAggregateExists() {
        when(eventStore.exists(anyString())).thenReturn(true);

        handler.on(new UserFollowerEvent.UserFollowedEvent("follow-1", "actor-1", "user-1", AT));

        verifyNoInteractions(commandGateway);
    }

    @Test
    void followFirstDelivery_dispatchesCreateCommand() {
        handler.on(new UserFollowerEvent.UserFollowedEvent("follow-2", "actor-1", "user-1", AT));

        verify(commandGateway).send(any());
    }
}
