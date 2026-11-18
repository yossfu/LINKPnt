package com.linkpoint.ui.media

import android.net.Uri
import com.google.common.collect.ImmutableMap
import com.linkpoint.R
import com.linkpoint.ui.settings.NotificationType

/**
 * Sonidos de notificación por defecto.
 *
 * Copia fiel de la implementación del proyecto `app/` del material
 * original (paquete adaptado a `com.linkpoint`): cada sonido apunta
 * a un recurso `raw/` y [defaultSounds] asocia cada
 * [NotificationType] con su sonido. Los 3 archivos `.m4a` ya están
 * en GitHub en `Linkpoint/src/main/res/raw/` (subidos por navegador,
 * commit 478d6e9), así que `R.raw.lumiya_*` existe en el repo.
 * Nota: en la copia local de trabajo (`ephora-viewer`) aún no existe
 * la carpeta `res/raw/`; hay que copiarla o tenerlo en cuenta al
 * subir: este archivo referencia esos recursos.
 */
enum class NotificationSounds(val resourceId: Int) {
    LocalChat(R.raw.lumiya_local_chat_message),
    IM(R.raw.lumiya_private_message),
    Group(R.raw.lumiya_group_message);

    fun getUri(): Uri {
        return getResourceUri(resourceId)
    }

    companion object {
        @JvmField
        val defaultSounds: ImmutableMap<NotificationType, NotificationSounds> = ImmutableMap.of(
            NotificationType.LocalChat, LocalChat,
            NotificationType.Private, IM,
            NotificationType.Group, Group
        )

        fun getResourceUri(i: Int): Uri {
            return Uri.parse("android.resource://com.linkpoint/$i")
        }
    }
}
