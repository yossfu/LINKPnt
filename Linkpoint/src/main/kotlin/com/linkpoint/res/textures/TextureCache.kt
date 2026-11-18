package com.linkpoint.res.textures

/**
 * STUB mínimo de la caché de texturas (`com.linkpoint.res.textures`).
 *
 * Este tipo no existe en el módulo Linkpoint: hay otros dos
 * `TextureCache` (`com.linkpoint.res.TextureCache` y
 * `com.linkpoint.texture.TextureCache`), pero ninguno en el paquete
 * `res.textures` que importan `GlobalOptions` y otros 8 archivos.
 * La implementación real está en el proyecto `app/` del material
 * original y es una clase grande (peticiones de recursos, caché de
 * texturas comprimidas, `setFetcher`, ejecutor descompresor...).
 *
 * Solo se expone lo que usa `GlobalOptions`: `getInstance`,
 * `onCacheDirChanged` y `setMaxTextureDownloads`, todos no-op. El
 * porte real de TextureCache es una tarea aparte y más grande.
 */
class TextureCache private constructor() {

    companion object {
        private val instance = TextureCache()

        @JvmStatic
        fun getInstance(): TextureCache = instance
    }

    fun onCacheDirChanged() {
        // STUB: sin caché real todavía.
    }

    fun setMaxTextureDownloads(count: Int) {
        // STUB: sin descargas todavía.
    }
}
