package com.linkpoint.voiceintf

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.linkpoint.slproto.users.ChatterID
import com.linkpoint.slproto.users.manager.UserManager
import com.linkpoint.voice.common.VoicePluginMessage
import com.linkpoint.voice.common.VoicePluginMessageType
import com.linkpoint.voice.common.messages.VoiceRinging
import com.linkpoint.voice.common.messages.VoiceSetAudioProperties
import com.linkpoint.voice.common.model.VoiceChannelInfo
import com.linkpoint.voice.common.model.VoiceLoginInfo

/**
 * Stub sin voz para Ephora Viewer.
 *
 * El archivo original era Java descompilado guardado como .kt y no
 * compilaba. La voz (plugin externo de Vivox, app aparte) NO está
 * implementada en este hito: la app debe compilar, instalar y abrir
 * la pantalla de login. Todos los métodos son no-op y
 * isPluginSupported() / checkPluginInstalled() devuelven false, así
 * que el resto del código desactiva la voz por su cuenta.
 *
 * Se mantiene exactamente la interfaz pública que usan los demás
 * archivos (GridConnectionService, SLVoice, GlobalOptions,
 * VoiceEnablePreference, UserFunctionsFragment, VoiceStatusView),
 * para no tocarlos. Cuando se implemente la voz de verdad, este
 * archivo se reemplaza por la implementación real.
 */
class VoicePluginServiceConnection(private val context: Context) : ServiceConnection {

    companion object {
        const val ACTION_VOICE_ACCEPT: String = "accept"
        const val ACTION_VOICE_REJECT: String = "reject"

        @JvmStatic
        fun isPluginSupported(): Boolean = false

        @JvmStatic
        fun checkPluginInstalled(context: Context): Boolean = false

        @JvmStatic
        fun shouldDisplayInstallOffer(): Boolean = false

        @JvmStatic
        fun setInstallOfferDisplayed(displayed: Boolean) {
            // Sin voz: no hay oferta que mostrar ni recordar.
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        // Sin voz: no hay servicio de plugin al que conectarse.
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        // Sin voz: nada que desconectar.
    }

    fun acceptCall(intent: Intent) {
        // Sin voz.
    }

    fun rejectCall(intent: Intent) {
        // Sin voz.
    }

    fun acceptVoiceCall(chatterID: ChatterID) {
        // Sin voz.
    }

    fun acceptVoiceCall(voiceRinging: VoiceRinging) {
        // Sin voz.
    }

    fun enableVoiceMic(enabled: Boolean) {
        // Sin voz.
    }

    fun disconnect() {
        // Sin voz.
    }

    fun terminateVoiceCall(chatterID: ChatterID) {
        // Sin voz.
    }

    fun setVoiceAudioProperties(properties: VoiceSetAudioProperties) {
        // Sin voz.
    }

    fun addChannel(chatterID: ChatterID, voiceChannelInfo: VoiceChannelInfo) {
        // Sin voz.
    }

    fun sendMessage(type: VoicePluginMessageType, message: VoicePluginMessage): Boolean = false

    fun setVoiceLoginInfo(voiceLoginInfo: VoiceLoginInfo?, userManager: UserManager?) {
        // Sin voz: no hay sesión de voz que iniciar.
    }
}
