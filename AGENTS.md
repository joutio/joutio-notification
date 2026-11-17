# AGENTS.md — joutio-notification

**Inbox par joueur** et préférences de notification, alimentées par les événements de domaine
des autres services. Package `com.joutio.notification`, base `joutio_notification`, port local
8093. Service headless : le BFF pousse et expose
([`docs/api/compte.md`](../../docs/api/compte.md#notifications)).

Lire : `../../AGENTS.md`, [`docs/backend/`](../../docs/backend/hexagonal.md).

## Agrégats

- **`NotificationAggregate`** :
  - `CreateNotificationCommand` (système) ;
  - `MarkNotificationRead/UnreadCommand` et `DeleteNotificationCommand` (destinataire seulement ;
    la suppression émet `NotificationDeletedEvent` puis `markDeleted()`) ;
  - fan-out sans état : `MarkAllNotificationsReadCommand`, `DeleteAllNotificationsCommand`.
- **`NotificationPreferenceAggregate`** : `UpdateNotificationPreferenceCommand`
  (`CREATE_IF_MISSING`).
  - Catégories `FOLLOW|ROOM|QUESTION` réglables, activées par défaut.
  - `ACCOUNT` est obligatoire et ignore les préférences.
  - Une notification d'une catégorie coupée n'est pas créée.
- Le service publie `NotificationCreatedEvent`, que le BFF pousse en STOMP, Web Push et push Expo.

## Ingestion (`NotificationIngestionHandler`, groupe `notification-ingestion`)

| Événement | Effet |
|---|---|
| `UserFollowedEvent` | `FOLLOW` pour le joueur suivi |
| `ChallengeCreatedEvent` | `CHALLENGE_RECEIVED` pour l'invité (`gameId` d'origine et expiration 10 s si revanche) |
| `ChallengeAcceptedEvent` | `ROOM_ACCEPTED` pour le lanceur (salle dérivée par `ChallengeRoomId`) ; invitation expirée |
| `ChallengeDeclinedEvent` | `CHALLENGE_DECLINED` pour le lanceur ; invitation expirée |
| `ChallengeCancelledEvent` | invitation **supprimée** (le BFF pousse `NOTIFICATION_DELETED`) |
| `ChallengeExpired/PurgedEvent` | invitation expirée |
| `RoomWizzedEvent` | `WIZZ` : une entrée par salle, remplacée à chaque relance |
| `RoomEnteredEvent`, `RoomCompleted/Cancelled/Expired/FailedEvent` | expirent la relance en attente ; `RoomCompletedEvent` rattache le `gameId` aux `ROOM_ACCEPTED` (lien vers l'arène après la purge de la salle) |
| `QuestionReportedEvent` | `QUESTION_REPORTED` pour le créateur du sujet, **anonyme** (sans acteur) |
| `UserSuspendedEvent` | `ACCOUNT_SUSPENDED` (catégorie `ACCOUNT`) |
| `TopicUnpublishedAfterAccountDeletionEvent` | `TOPIC_DRAFTED` pour le propriétaire du sujet |

- Les autres événements de salle et l'appariement public ne vont pas dans l'inbox (temps réel
  seulement).
- **Idempotence** :
  - `notificationId = UUID(type:sourceId:userId)`, avec garde d'existence et contrainte unique
    `uq_notification_source` ;
  - une relecture postérieure à une suppression est ignorée (lecture de l'event store), sinon
    `AggregateStreamCreationException` empoisonnerait le processor.
- Chaque événement porte ses destinataires : aucun index de routage, aucune requête sortante.

## Lectures fournies

`GetNotificationsQuery`, `GetNotificationQuery`, `CountUnreadNotificationsQuery`,
`GetUnreadNotificationIdsQuery`, `GetNotificationPreferencesQuery`. Read models
`notification_entry` et `notification_preference_entry`.

Contrats consommés : `joutio-social-domain`, `joutio-matchmaking-domain`, `joutio-theme-domain`,
`joutio-identity-domain`.

## Commandes

```bash
mvn -q -pl joutio-notification-domain test
mvn -pl joutio-notification-infrastructure test
```

## Pièges

- `NotificationIngestionHandler` lit l'event store directement : ce n'est pas conforme à la règle
  de l'adaptateur dédié (`docs/ROADMAP.md`, Dette).
