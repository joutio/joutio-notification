package com.joutio.notification.domain.port.out;

/** Port sortant du flux d'événements des notifications (event store de l'agrégat). */
public interface NotificationEventStorePort {

    /** {@code true} si l'agrégat de la notification a déjà au moins un événement. */
    boolean exists(String notificationId);
}
