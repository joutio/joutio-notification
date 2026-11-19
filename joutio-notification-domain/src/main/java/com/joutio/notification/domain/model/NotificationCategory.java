package com.joutio.notification.domain.model;

/** Catégorie de préférence de notification (un toggle côté Réglages). */
public enum NotificationCategory {
    FOLLOW,
    ROOM,
    /** Revanches : défi nominatif lancé depuis le résultat d'une partie (porte son {@code gameId}). */
    REMATCH,
    /** Signalements de questions déposés sur les sujets du joueur. */
    QUESTION,
    /** Informations obligatoires sur le compte (suspension). */
    ACCOUNT
}
