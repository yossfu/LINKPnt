package com.linkpoint

import android.app.Service
import android.content.Intent
import android.os.IBinder
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
 * Adelgazado para el cierre de login (2026-11-18): la versión anterior del
 * stub importaba SLGridConnection, ChatterID, UserManager y tipos de voz,
 * archivos de 400-600 líneas en Java descompilado que arrastraban medio
 * árbol al conjunto de compilación. Esos métodos (getGridConnection(),
 * acceptVoiceCall(), startVoice(), etc.) y su API pública volverán cuando se
 * implemente la conexión real; ningún archivo del cierre de login los usa.
 * Solo se conserva lo que necesita el arranque: la acción de login, la
 * instancia del servicio y el estado de actividades visibles.
 */
class GridConnectionService : Service() {

    companion object {
        const val LOGIN_ACTION: String = "com.linkpoint.ACTION_LOGIN"

        private var serviceInstance: WeakReference<GridConnectionService>? = null

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
}
