package com.linkpoint.res.mesh

/**
 * STUB mínimo de la caché de mallas.
 *
 * El tipo `com.linkpoint.res.mesh.MeshCache` no existe en el módulo
 * Linkpoint (la implementación real está en el proyecto `app/` del
 * material original, donde extiende `ResourceFileCache`). Solo se
 * expone lo que usa `GlobalOptions` (`onCacheDirChanged`) y
 * `setCapURL` del original. `GeometryCache` lo usa además como tipo
 * y le pide recursos (`RequestResource`): eso queda para el porte
 * real de MeshCache, que debe extender `ResourceFileCache`.
 */
object MeshCache {

    fun onCacheDirChanged() {
        // STUB: nada persistente que limpiar.
    }

    fun setCapURL(url: String?) {
        // STUB: sin URL de capability todavía.
    }
}
