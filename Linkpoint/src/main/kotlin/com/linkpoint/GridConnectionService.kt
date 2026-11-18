package com.linkpoint

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.linkpoint.slproto.SLGridConnection
import com.linkpoint.slproto.users.ChatterID
import com.linkpoint.slproto.users.manager.UserManager
import com.linkpoint.voice.common.messages.VoiceRinging
import com.linkpoint.voice.common.model.VoiceLoginInfo
import com.linkpoint.voiceintf.VoicePluginServiceConnection
import java.lang.ref.WeakReference

/**
 * STUB TEMPORAL — sin conexión real a Second Life.
 *
 * Sustituye al GridConnectionService original (1.643 líneas de Java descompilado
 * que no compilaba como Kotlin). Este stub existe solo para el hito de la
 * pantalla de login: que el APK debug compile, instale y abra la pantalla de
 * login. NO hay conexión UDP, ni circuito, ni login real todavía; eso es una
 * fase posterior y se implementará de verdad, no simulado.
 *
 * Mantiene la API pública que usan los llamadores del árbol (LoginActivity,
 * ConnectedActivity, TeleportSLURLActivity, SLVoice, VoiceStatusView y
 * SLAgentCircuit): la acción de login, getGridConnection(), getServiceInstance(),
 * hasVisibleActivities() y los métodos de voz delegados al plugin. Todos
 * devuelven valores seguros (null / false) y no hacen nada.
 */
class GridConnectionService : Service() {

    companion object {
        const val LOGIN_ACTION: String = "com.linkpoint.ACTION_LOGIN"

        private var serviceInstance: WeakReference<GridConnectionService>? = null

        /**
         * Sin conexión real: siempre null. Los llamadores ya comprueban null
         * antes de usar la conexión.
         */
        @JvmStatic
        fun getGridConnection(): SLGridConnection? = null

        @JvmStatic
        fun getServiceInstance(): GridConnectionService? = serviceInstance?.get()

        @JvmStatic
        fun hasVisibleActivities(): Boolean = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        serviceInstance = WeakReference(this)
    }

    override fun onDestroy() {
        serviceInstance = null
        super.onDestroy()
    }

    /**
     * Se ignora el intent: sin conexión no hay login que iniciar ni llamada
     * de voz que aceptar/rechazar. Se devuelve START_STICKY para que el
     * servicio no muera por el arranque desde LoginActivity.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // --- Voz (sin voz todavía; todo no-op, como el stub de VoicePluginServiceConnection) ---

    fun acceptVoiceCall(chatterID: ChatterID) {
        // Sin voz todavía.
    }

    fun acceptVoiceCall(voiceRinging: VoiceRinging) {
        // Sin voz todavía.
    }

    fun enableVoiceMic(enabled: Boolean) {
        // Sin voz todavía.
    }

    /**
     * Sin plugin de voz conectado: siempre null. Los llamadores ya comprueban null.
     */
    fun getVoicePluginServiceConnection(): VoicePluginServiceConnection? = null

    fun startVoice(voiceLoginInfo: VoiceLoginInfo, userManager: UserManager) {
        // Sin voz todavía.
    }

    fun stopVoice() {
        // Sin voz todavía.
    }

    fun terminateVoiceCall(chatterID: ChatterID) {
        // Sin voz todavía.
    }
}
