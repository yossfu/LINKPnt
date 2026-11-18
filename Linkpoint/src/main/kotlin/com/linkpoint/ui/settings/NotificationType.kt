package com.linkpoint.ui.settings

import com.google.common.collect.ImmutableList

/**
 * Tipos de notificación (chat local, IM privado, mensaje de grupo).
 *
 * Reescrito en Kotlin real a partir del Java descompilado (campos
 * `private String x` con constructor Java, bloque `static`, etc.).
 * Cada entrada guarda sus claves de preferencia; [VALUES] es la
 * lista en orden de declaración y
 * [VALUES_BY_DESCENDING_PRIORITY] en orden Private, Group, LocalChat.
 * Se mantienen los getters `get*Key()` / `getPriority()` que usan
 * los demás archivos.
 */
enum class NotificationType(
    val priority: Int,
    val prefScreenKey: String,
    val enableKey: String,
    val playSoundKey: String,
    val ringtoneKey: String,
    val blinkKey: String,
    val blinkColorKey: String
) {
    LocalChat(0, "notify_local_chat", "enableNotifyLocalChat", "soundOnLocalChat", "notifySoundLocalChat", "notifyLEDchatIMs", "notifyLEDColorChatIMs"),
    Private(2, "notify_private_im", "enableNotifyPrivateIM", "soundOnPrivateIM", "notifySoundPrivateIM", "notifyLEDprivateIMs", "notifyLEDColorPrivateIMs"),
    Group(1, "notify_group_messages", "enableNotifyGroupMessage", "soundOnGroupMessage", "notifySoundGroupMessage", "notifyLEDgroupMessages", "notifyLEDColorGroupMessages");

    fun getBlinkColorKey(): String {
        return blinkColorKey
    }

    fun getBlinkKey(): String {
        return blinkKey
    }

    fun getEnableKey(): String {
        return enableKey
    }

    fun getPlaySoundKey(): String {
        return playSoundKey
    }

    fun getPrefScreenKey(): String {
        return prefScreenKey
    }

    fun getPriority(): Int {
        return priority
    }

    fun getRingtoneKey(): String {
        return ringtoneKey
    }

    companion object {
        @JvmField
        val VALUES: ImmutableList<NotificationType> = ImmutableList.copyOf(entries)

        @JvmField
        val VALUES_BY_DESCENDING_PRIORITY: ImmutableList<NotificationType> = ImmutableList.of(Private, Group, LocalChat)
    }
}
