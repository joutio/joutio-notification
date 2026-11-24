package com.joutio.notification.application.handler.event;

import com.joutio.identity.domain.event.UserEvent;
import com.joutio.matchmaking.domain.event.RoomEvent;
import com.joutio.matchmaking.domain.model.RoomReminderStage;
import com.joutio.notification.domain.command.NotificationCommand;
import com.joutio.notification.domain.model.Notification;
import com.joutio.notification.domain.model.NotificationCategory;
import com.joutio.notification.domain.model.NotificationPreference;
import com.joutio.notification.domain.model.NotificationType;
import com.joutio.notification.domain.port.out.NotificationEventStorePort;
import com.joutio.notification.domain.port.out.NotificationPreferenceRepositoryPort;
import com.joutio.notification.domain.port.out.NotificationRepositoryPort;
import com.joutio.social.domain.event.UserFollowerEvent;
import com.joutio.theme.domain.event.QuestionReportEvent;
import com.joutio.theme.domain.event.TopicEvent.TopicUnpublishedAfterAccountDeletionEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateStreamCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Ingestion des événements de domaine (Kafka) → notifications personnelles.
 * <p>
 * L'identifiant de notification est déterministe (type + source + destinataire) : la relecture
 * at-least-once est idempotente (contrainte unique en base).
 * <p>
 * Salles : seuls l'invitation d'un défi (envoi, relance, acceptation, refus, fin) et l'appel d'un joueur absent
 * (« wizz ») passent par l'inbox, avec {@code sourceId = roomId}. La présence, l'avancement de la préparation et
 * l'échec sont poussés en temps réel par le BFF ; {@code ROOM_COMPLETED} rattache le {@code gameId} à la notification
 * d'acceptation (deep link arène, la salle étant purgée après rétention).
 */
@Component
@ProcessingGroup("notification-ingestion")
public class NotificationIngestionHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationIngestionHandler.class);

    private final CommandGateway commandGateway;
    private final NotificationRepositoryPort notificationRepository;
    private final NotificationPreferenceRepositoryPort preferenceRepository;
    private final NotificationEventStorePort eventStore;

    public NotificationIngestionHandler(CommandGateway commandGateway,
                                        NotificationRepositoryPort notificationRepository,
                                        NotificationPreferenceRepositoryPort preferenceRepository,
                                        NotificationEventStorePort eventStore) {
        this.commandGateway = commandGateway;
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.eventStore = eventStore;
    }

    // ============================== Follows ==============================

    @EventHandler
    public void on(UserFollowerEvent.UserFollowedEvent event) {
        createIfAllowed(event.followedId(), NotificationType.FOLLOW, event.followerId(),
                event.followId(), null, null, null);
    }

    // ============================== Compte ==============================

    /**
     * Suspension d'un compte : information obligatoire, indépendante des préférences.
     * La notification est conservée dans l'inbox pour être lue après réactivation.
     */
    @EventHandler
    public void on(UserEvent.UserSuspendedEvent event) {
        createIfAllowed(event.userId(), NotificationType.ACCOUNT_SUSPENDED,
                null, event.suspensionId(), null, null, null);
    }

    // ========================= Signalements de questions =========================

    /**
     * Un joueur a signalé une question d'un sujet du destinataire. Une notification par
     * signalement ({@code sourceId = reportId}) : après réactivation, un nouveau signalement
     * produit bien une nouvelle alerte. <b>Anonyme</b> : aucun {@code actorId}, le signalant n'est
     * jamais révélé à l'équipe du sujet.
     */
    @EventHandler
    public void on(QuestionReportEvent.QuestionReportedEvent event) {
        if (event.topicCreatorId() == null) {
            return;
        }
        createIfAllowed(event.topicCreatorId(), NotificationType.QUESTION_REPORTED,
                null, event.reportId(), event.topicId(), null, null);
    }

    /** Un sujet publié est repassé en DRAFT après suppression d'un compte. */
    @EventHandler
    public void on(TopicUnpublishedAfterAccountDeletionEvent event) {
        createIfAllowed(event.ownerId(), NotificationType.TOPIC_DRAFTED,
                null, event.topicId(), event.topicId(), null, null);
    }

    // ======================== Salles et défis (sourceId = roomId) ========================

    /**
     * Défi ou revanche envoyé : la salle naît {@code INVITED} et l'invité reçoit l'invitation. {@code gameId} porte la
     * partie d'origine d'une revanche (catégorie {@code REMATCH}).
     */
    @EventHandler
    public void on(RoomEvent.RoomCreatedEvent event) {
        if (!event.origin().requiresAcceptance()) {
            return;
        }
        createIfAllowed(event.opponentId(), NotificationType.CHALLENGE_RECEIVED, event.initiatorId(),
                event.roomId(), event.topicId(), event.sourceGameId(), event.expiresAt());
    }

    /**
     * Relance, une entrée unique par salle et par étape : la précédente est supprimée (push
     * {@code NOTIFICATION_DELETED}) puis recréée non lue, en tête (nouveau toast, nouveau push). L'identifiant
     * déterministe inclut l'horodatage de la relance pour que la relecture Kafka reste reproductible.
     * <ul>
     *   <li>invitation : l'invité reçoit une nouvelle {@code CHALLENGE_RECEIVED} ;</li>
     *   <li>présence : l'absent reçoit un {@code WIZZ}.</li>
     * </ul>
     */
    @EventHandler
    public void on(RoomEvent.RoomRemindedEvent event) {
        boolean invitation = event.stage() == RoomReminderStage.INVITATION;
        NotificationType type = invitation ? NotificationType.CHALLENGE_RECEIVED : NotificationType.WIZZ;
        deleteAll(event.roomId(), type);
        createIfAllowed(event.targetId(), type, event.playerId(), event.roomId(),
                invitation ? event.topicId() : null,
                invitation ? event.sourceGameId() : null,
                invitation ? event.expiresAt() : null,
                deterministicId(type, event.roomId() + "@" + event.remindedAt().toEpochMilli(), event.targetId()));
    }

    /** Défi accepté : l'invitation n'est plus actionnable, le lanceur est prévenu. */
    @EventHandler
    public void on(RoomEvent.RoomAcceptedEvent event) {
        notificationRepository.expireBySourceIdAndType(
                event.roomId(), NotificationType.CHALLENGE_RECEIVED, event.acceptedAt());
        createIfAllowed(event.initiatorId(), NotificationType.ROOM_ACCEPTED, event.opponentId(),
                event.roomId(), event.topicId(), null, null);
    }

    @EventHandler
    public void on(RoomEvent.RoomDeclinedEvent event) {
        notificationRepository.expireBySourceIdAndType(
                event.roomId(), NotificationType.CHALLENGE_RECEIVED, event.declinedAt());
        createIfAllowed(event.initiatorId(), NotificationType.CHALLENGE_DECLINED, event.opponentId(),
                event.roomId(), null, null, null);
    }

    /** Le joueur visé par une relance vient d'apparaître : la relance est honorée (trace expirée). */
    @EventHandler
    public void on(RoomEvent.RoomEnteredEvent event) {
        notificationRepository.expireBySourceIdAndTypeAndUserId(
                event.roomId(), NotificationType.WIZZ, event.playerId(), event.enteredAt());
    }

    @EventHandler
    public void on(RoomEvent.RoomCompletedEvent event) {
        // Deep link : les notifications d'acceptation pointent vers la partie créée — l'inbox
        // peut rejoindre l'arène même après la purge de la salle (rétention 2 min).
        notificationRepository.attachGameId(event.roomId(), event.gameId());
        notificationRepository.expireBySourceIdAndType(
                event.roomId(), NotificationType.WIZZ, event.completedAt());
    }

    /**
     * Salle annulée (bouton ou blocage) : l'invitation est <b>supprimée</b> pour que le client ferme la modale et
     * retire la carte en temps réel (push {@code NOTIFICATION_DELETED}) ; la relance en attente expire.
     */
    @EventHandler
    public void on(RoomEvent.RoomCancelledEvent event) {
        deleteAll(event.roomId(), NotificationType.CHALLENGE_RECEIVED);
        notificationRepository.expireBySourceIdAndType(
                event.roomId(), NotificationType.WIZZ, event.cancelledAt());
    }

    /** Invitation sans réponse ou salle jamais lancée : rien n'est plus actionnable. */
    @EventHandler
    public void on(RoomEvent.RoomExpiredEvent event) {
        expireActionable(event.roomId(), event.expiredAt());
    }

    /** Échec de préparation : la relance en attente n'est plus actionnable. */
    @EventHandler
    public void on(RoomEvent.RoomFailedEvent event) {
        notificationRepository.expireBySourceIdAndType(
                event.roomId(), NotificationType.WIZZ, event.failedAt());
    }

    /** Filet : la purge suit normalement un état terminal déjà traité. */
    @EventHandler
    public void on(RoomEvent.RoomPurgedEvent event) {
        expireActionable(event.roomId(), event.purgedAt());
    }

    private void expireActionable(String roomId, Instant at) {
        notificationRepository.expireBySourceIdAndType(roomId, NotificationType.CHALLENGE_RECEIVED, at);
        notificationRepository.expireBySourceIdAndType(roomId, NotificationType.WIZZ, at);
    }

    // =====================================================================

    /** Supprime les entrées d'une source (relance remplacée, invitation annulée). */
    private void deleteAll(String sourceId, NotificationType type) {
        for (Notification previous : notificationRepository.findBySourceIdAndType(sourceId, type)) {
            try {
                commandGateway.sendAndWait(new NotificationCommand.DeleteNotificationCommand(
                        previous.notificationId(), previous.userId()));
            } catch (RuntimeException error) {
                // Déjà supprimée (course avec l'utilisateur) : plus rien à faire.
                logger.debug("Notification {} déjà absente à la suppression: {}",
                        previous.notificationId(), error.toString());
            }
        }
    }

    private void createIfAllowed(String userId,
                                 NotificationType type,
                                 String actorId,
                                 String sourceId,
                                 String topicId,
                                 String gameId,
                                 Instant expiresAt) {
        createIfAllowed(userId, type, actorId, sourceId, topicId, gameId, expiresAt,
                deterministicId(type, sourceId, userId));
    }

    private void createIfAllowed(String userId,
                                 NotificationType type,
                                 String actorId,
                                 String sourceId,
                                 String topicId,
                                 String gameId,
                                 Instant expiresAt,
                                 String notificationId) {
        if (userId == null || sourceId == null) {
            return;
        }
        // Les informations de compte (suspension) et la perte de publication d'un sujet sont
        // obligatoires : elles ignorent les préférences.
        if (type != NotificationType.ACCOUNT_SUSPENDED
                && type != NotificationType.TOPIC_DRAFTED
                && !isEnabled(userId, type.category(gameId))) {
            logger.debug("Notification {} ignorée (préférence {} désactivée) pour {}",
                    type, type.category(gameId), userId);
            return;
        }
        // Le read model seul ne suffit pas : après une suppression (hard delete), une relecture
        // at-least-once (replay Kafka) ne voit plus la ligne et retenterait de créer un agrégat
        // existant → poison pill. L'event store est la garde d'idempotence durable.
        if (notificationRepository.existsById(notificationId) || aggregateExists(notificationId)) {
            return;
        }
        try {
            commandGateway.send(new NotificationCommand.CreateNotificationCommand(
                    notificationId, userId, type, actorId, sourceId, topicId, gameId, expiresAt));
        } catch (RuntimeException error) {
            if (isAggregateAlreadyCreated(error)) {
                logger.warn("Notification {} déjà créée (agrégat existant) — relecture ignorée",
                        notificationId);
                return;
            }
            throw error;
        }
    }

    private boolean aggregateExists(String notificationId) {
        return eventStore.exists(notificationId);
    }

    private static boolean isAggregateAlreadyCreated(Throwable error) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (current instanceof AggregateStreamCreationException) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnabled(String userId, NotificationCategory category) {
        return preferenceRepository.find(userId, category)
                .map(NotificationPreference::enabled)
                .orElse(true);
    }

    private static String deterministicId(NotificationType type, String sourceId, String userId) {
        return UUID.nameUUIDFromBytes(
                        (type + ":" + sourceId + ":" + userId).getBytes(StandardCharsets.UTF_8))
                .toString();
    }
}
