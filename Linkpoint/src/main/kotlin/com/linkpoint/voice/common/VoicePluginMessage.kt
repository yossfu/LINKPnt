package com.linkpoint.voice.common

import android.os.Bundle

/**
 * Interfaz base de los mensajes del plugin de voz.
 *
 * Faltaba en el material original: todas las clases de mensajes
 * (VoiceAcceptCall, VoiceConnectChannel, VoiceRinging, etc.)
 * la implementan y sobrescriben toBundle(), pero ningún archivo
 * la definía, así que ninguna compilaba. Se restaura aquí con
 * el único miembro que usan: toBundle().
 */
interface VoicePluginMessage {
    fun toBundle(): Bundle
}
