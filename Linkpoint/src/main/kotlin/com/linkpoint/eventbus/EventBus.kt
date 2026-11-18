package com.linkpoint.eventbus

import android.app.Activity

/**
 * STUB mínimo del bus de eventos (sin entrega todavía).
 *
 * El tipo `com.linkpoint.eventbus.EventBus` no existe en el módulo
 * Linkpoint (la implementación real está en el proyecto `app/` del
 * material original, paquete `com.lumiyaviewer.lumiya`, que no se
 * subió). `GlobalOptions` y muchos archivos más lo usan, así que se
 * crea este stub con exactamente la API que usan los llamadores
 * (`getInstance`, `publish`, `subscribe`, `unsubscribe`,
 * `unsubscribeActivity`) para que el código compile. Todos los
 * métodos son no-op: los eventos se publican pero nadie los recibe
 * hasta que se porte la implementación real.
 */
class EventBus private constructor() {

    companion object {
        @JvmStatic
        fun getInstance(): EventBus = InstanceHolder.instance

        private object InstanceHolder {
            val instance = EventBus()
        }
    }

    fun publish(event: Any) {
        // STUB: sin suscriptores todavía.
    }

    fun subscribe(subscriber: Any) {
        // STUB: sin registro todavía.
    }

    fun subscribe(activity: Activity) {
        // STUB: sin registro todavía.
    }

    fun unsubscribe(subscriber: Any) {
        // STUB: sin registro todavía.
    }

    fun unsubscribeActivity(activity: Activity) {
        // STUB: sin registro todavía.
    }
}
