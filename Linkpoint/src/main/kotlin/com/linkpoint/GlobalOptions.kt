package com.linkpoint

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.common.base.Strings
import com.google.common.collect.ImmutableList
import com.linkpoint.eventbus.EventBus
import com.linkpoint.render.TextureMemoryTracker
import com.linkpoint.res.mesh.MeshCache
import com.linkpoint.res.textures.TextureCache
import com.linkpoint.ui.media.NotificationSounds
import com.linkpoint.ui.settings.NotificationType
import com.linkpoint.ui.settings.ThemeChangedEvent
import com.linkpoint.voiceintf.VoicePluginServiceConnection
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Opciones globales de Ephora Viewer, leídas de SharedPreferences.
 *
 * Reescrito en Kotlin real a partir del Java descompilado (que no
 * compilaba: declaraciones `private Boolean x`, `private Int x`,
 * `val Int getX()` fuera de propiedades, constructores y `switch`
 * Java, `catch (Exception e)`, variables sin declarar, etc.).
 *
 * Interfaz pública preservada: [getInstance], todos los getters
 * `get*` / `is*`, [getCacheDir], [enableVoice], [initialize],
 * [updateFromPreferences], [onSharedPreferenceChanged], la clase
 * anidada [GlobalOptionsChangedEvent] y el enum [MeshRendering].
 * `meshRendering` se expone además como propiedad pública (con
 * setter privado) porque `MeshData` lo lee como
 * `GlobalOptions.getInstance().meshRendering` y `MeshRendering.lodName`
 * como propiedad; el getter `getMeshRendering()` lo genera Kotlin
 * automáticamente a partir de la propiedad.
 *
 * Temas: los identificadores del código descompilado
 * (`R.style.f47Theme.Linkpoint.Light` y compañía) no corresponden a
 * ningún estilo de `res/`; se mapean a los estilos que sí existen:
 * claro -> `Theme.Lumiya.Light`, oscuro -> `Theme.Linkpoint`,
 * rosa -> `Theme.Lumiya.Pink`.
 */
class GlobalOptions : OnSharedPreferenceChangeListener {
    private var RLVEnabled: Boolean = false
    private var advancedRendering: Boolean = true
    private var autoReconnect: Boolean = true
    private val availableCacheDirs: AtomicReference<ImmutableList<File>> = AtomicReference(ImmutableList.of())
    private val baseCacheDir: AtomicReference<File> = AtomicReference()
    private val cacheDirUsed: AtomicBoolean = AtomicBoolean(false)
    private var cloudSyncEnabled: Boolean = false
    private var compressedTextures: Boolean = true
    private var forceDaylightHour: Float = 0.5f
    private var forceDaylightTime: Boolean = false
    private var highQualityTextures: Boolean = false
    private var hoverTextEnableHUDs: Boolean = true
    private var hoverTextEnableObjects: Boolean = false
    private var keepWifiOn: Boolean = false
    private var legacyUserNames: Boolean = false
    private var maxReconnectAttempts: Int = 10
    private var maxTextureDownloads: Int = 2
    var meshRendering: MeshRendering = MeshRendering.medium
        private set
    private var renderClouds: Boolean = true
    private var showTimestamps: Boolean = true
    private var terrainTextures: Boolean = true
    private var themeResourceId: Int = R.style.Theme_Lumiya_Light
    private var useFXAA: Boolean = false
    private var voiceEnabled: Boolean = false

    class GlobalOptionsChangedEvent(val preferences: SharedPreferences)

    private object InstanceHolder {
        val instance: GlobalOptions = GlobalOptions()
    }

    enum class MeshRendering(val lodName: String?) {
        high("high_lod"),
        medium("medium_lod"),
        low("low_lod"),
        lowest("lowest_lod"),
        disabled(null);

        fun getLODName(): String? {
            return lodName
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): GlobalOptions {
            return InstanceHolder.instance
        }

        private fun getTotalMemory(): Long {
            var total: Long = 0
            try {
                val bufferedReader = BufferedReader(FileReader("/proc/meminfo"), 8192)
                var readLine: String? = null
                while (true) {
                    readLine = bufferedReader.readLine()
                    if (readLine == null || readLine.startsWith("MemTotal:")) {
                        break
                    }
                }
                if (readLine != null) {
                    val split: Array<String> = readLine.split("\\s+".toRegex()).toTypedArray()
                    if (split.size >= 2) {
                        total = split[1].toLong()
                    }
                    for (i in split.indices) {
                        Debug.Log("Memory $i:${split[i]}")
                    }
                }
                bufferedReader.close()
            } catch (e: Exception) {
            }
            return total
        }
    }

    private fun isCacheDirectoryWriteable(file: File?): Boolean {
        if (file == null) {
            return false
        }
        try {
            file.mkdirs()
            if (!file.exists()) {
                return false
            }
            val tmpFile = File(file, ".tmp")
            if (tmpFile.exists()) {
                tmpFile.delete()
                if (tmpFile.exists()) {
                    return false
                }
            }
            tmpFile.createNewFile()
            if (!tmpFile.exists()) {
                return false
            }
            tmpFile.delete()
            return true
        } catch (e: IOException) {
            return false
        }
    }

    private fun updateCacheDir(context: Context, sharedPreferences: SharedPreferences) {
        val previousBaseCacheDir: File? = baseCacheDir.get()
        val location: String = sharedPreferences.getString("cache_location", "") ?: ""
        // Directorio elegido: el anterior si sigue siendo escribible y no
        // hay una ubicación personalizada; si no, el caché de la app.
        // El bucle del código descompilado reasignaba `file`/`cacheDir`
        // de forma que su único efecto neto era este, y la rama de
        // ubicación personalizada estaba vacía, así que la ubicación
        // personalizada queda sin efecto, igual que en el descompilado.
        // (Probable pérdida del descompilador; revisar más adelante.)
        val chosenDir: File? =
            if (previousBaseCacheDir != null && isCacheDirectoryWriteable(previousBaseCacheDir) && location.isEmpty()) {
                previousBaseCacheDir
            } else {
                null
            }
        val candidates = ArrayList<File>()
        val externalCacheDirs: Array<File>? = ContextCompat.getExternalCacheDirs(context)
        if (externalCacheDirs != null) {
            for (dir in externalCacheDirs) {
                if (dir != null) {
                    candidates.add(dir)
                }
            }
        }
        val appCacheDir: File? = context.cacheDir
        if (appCacheDir != null) {
            candidates.add(appCacheDir)
        }
        val builder = ImmutableList.builder<File>()
        for (candidate in candidates) {
            Debug.Printf("Cache: checking cache location %s", candidate)
            if (isCacheDirectoryWriteable(candidate)) {
                builder.add(candidate)
            }
        }
        val cacheDir: File = chosenDir ?: context.cacheDir
        Debug.Printf("Cache: cache location set to %s", cacheDir.absolutePath)
        this.availableCacheDirs.set(builder.build())
        this.baseCacheDir.set(cacheDir)
        try {
            cacheDir.mkdirs()
            if (cacheDir.exists()) {
                File(cacheDir, ".nomedia").createNewFile()
            }
        } catch (e: Exception) {
        }
        if (previousBaseCacheDir != null && previousBaseCacheDir != cacheDir) {
            Debug.Printf("Cache: Cache location has been changed.")
            TextureCache.getInstance().onCacheDirChanged()
            MeshCache.onCacheDirChanged()
        }
    }

    private fun updateNotificationSoundDefault(sharedPreferences: SharedPreferences, notificationType: NotificationType) {
        if (!sharedPreferences.contains(notificationType.getRingtoneKey())) {
            val notificationSounds: NotificationSounds? = NotificationSounds.defaultSounds[notificationType]
            if (notificationSounds != null) {
                val uri: Uri = notificationSounds.getUri()
                val edit: Editor = sharedPreferences.edit()
                edit.putString(notificationType.getRingtoneKey(), uri.toString())
                edit.apply()
                Debug.Printf("NotificationSounds: Updated %s preference to %s", notificationType.getRingtoneKey(), uri)
            }
        }
    }

    fun enableVoice() {
        val edit: Editor = LinkpointApp.getDefaultSharedPreferences().edit()
        edit.putBoolean("enableVoice", true)
        edit.apply()
    }

    fun getAdvancedRendering(): Boolean {
        return this.advancedRendering
    }

    fun getAutoReconnect(): Boolean {
        return this.autoReconnect
    }

    fun getAvailableCacheDirs(): ImmutableList<File> {
        return this.availableCacheDirs.get()
    }

    fun getBaseCacheDir(): File {
        // Los llamadores (SettingsFragment) lo asignan a un File no nulo;
        // si aún no se inicializó, se devuelve el caché de la app, igual
        // que hace getCacheDir(String).
        return this.baseCacheDir.get() ?: LinkpointApp.getContext().cacheDir
    }

    fun getCacheDir(str: String): File {
        this.cacheDirUsed.set(true)
        val base: File? = this.baseCacheDir.get()
        val parent: File = base ?: LinkpointApp.getContext().cacheDir
        val dir = File(parent, str)
        try {
            dir.mkdirs()
        } catch (e: Exception) {
        }
        return dir
    }

    fun getCompressedTextures(): Boolean {
        return this.compressedTextures
    }

    fun getForceDaylightHour(): Float {
        return this.forceDaylightHour
    }

    fun getForceDaylightTime(): Boolean {
        return this.forceDaylightTime
    }

    fun getHighQualityTextures(): Boolean {
        return this.highQualityTextures
    }

    fun getHoverTextEnableHUDs(): Boolean {
        return this.hoverTextEnableHUDs
    }

    fun getHoverTextEnableObjects(): Boolean {
        return this.hoverTextEnableObjects
    }

    fun getKeepWifiOn(): Boolean {
        return this.keepWifiOn
    }

    fun getMaxReconnectAttempts(): Int {
        return this.maxReconnectAttempts
    }

    fun getMaxTextureDownloads(): Int {
        return this.maxTextureDownloads
    }

    fun getRLVEnabled(): Boolean {
        return this.RLVEnabled
    }

    fun getRenderClouds(): Boolean {
        return this.renderClouds
    }

    fun getShowTimestamps(): Boolean {
        return this.showTimestamps
    }

    fun getTerrainTextures(): Boolean {
        return this.terrainTextures
    }

    fun getThemeResourceId(): Int {
        return this.themeResourceId
    }

    fun getUseFXAA(): Boolean {
        return this.useFXAA
    }

    fun getVoiceEnabled(): Boolean {
        return this.voiceEnabled
    }

    fun initialize() {
        val defaultSharedPreferences: SharedPreferences = LinkpointApp.getDefaultSharedPreferences()
        updateFromPreferences(LinkpointApp.getContext(), defaultSharedPreferences)
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun isCacheDirUsed(): Boolean {
        return this.cacheDirUsed.get()
    }

    fun isLegacyUserNames(): Boolean {
        return this.legacyUserNames
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        updateFromPreferences(LinkpointApp.getContext(), sharedPreferences)
        EventBus.getInstance().publish(GlobalOptionsChangedEvent(sharedPreferences))
    }

    fun updateFromPreferences(context: Context, sharedPreferences: SharedPreferences) {
        Debug.Printf("Updating options from preferences.")
        updateNotificationSoundDefault(sharedPreferences, NotificationType.Private)
        updateNotificationSoundDefault(sharedPreferences, NotificationType.Group)
        updateNotificationSoundDefault(sharedPreferences, NotificationType.LocalChat)
        // `i` se reutiliza en varios bloques (límites de memoria/texturas);
        // en el descompilado aparecía sin declarar.
        var i = 0
        if (!sharedPreferences.getBoolean("system_defaults_set", false)) {
            val edit: Editor = sharedPreferences.edit()
            val totalMemory: Long = getTotalMemory()
            val availableProcessors: Int = Runtime.getRuntime().availableProcessors()
            if (availableProcessors < 2 || totalMemory <= 524288) {
                edit.putBoolean("high_quality_textures", false)
            } else {
                edit.putBoolean("high_quality_textures", true)
            }
            i = 64
            if (totalMemory != 0L) {
                i = if (totalMemory <= 262144) 32 else if (totalMemory <= 524288) 64 else 128
            }
            edit.putString("texture_memory_limit", i.toString())
            i = 2
            if (availableProcessors >= 4 && totalMemory > 524288) {
                i = 8
            } else if (availableProcessors >= 2) {
                i = 4
            }
            edit.putString("max_texture_downloads", i.toString())
            edit.putBoolean("system_defaults_set", true)
            edit.commit()
        }
        val previousThemeResourceId: Int = this.themeResourceId
        val themeName: String = Strings.nullToEmpty(sharedPreferences.getString("theme", "light"))
        this.themeResourceId = when (themeName) {
            "dark" -> R.style.Theme_Lumiya
            "pink" -> R.style.Theme_Lumiya_Pink
            else -> R.style.Theme_Lumiya_Light
        }
        this.legacyUserNames = sharedPreferences.getBoolean("legacyUserNames", false)
        this.showTimestamps = sharedPreferences.getBoolean("chatTimestamps", true)
        this.highQualityTextures = sharedPreferences.getBoolean("high_quality_textures", false)
        this.compressedTextures = sharedPreferences.getBoolean("compressed_textures", true)
        this.keepWifiOn = sharedPreferences.getBoolean("keep_wifi_on", true)
        this.cloudSyncEnabled = sharedPreferences.getBoolean("sync_to_gdrive", false)
        this.voiceEnabled = if (sharedPreferences.getBoolean("enableVoice", false)) VoicePluginServiceConnection.isPluginSupported() else false
        try {
            i = Integer.parseInt(sharedPreferences.getString("max_texture_downloads", "2") ?: "2")
            if (i < 1) {
                i = 1
            }
        } catch (e: Exception) {
            i = 2
        }
        if (i != this.maxTextureDownloads) {
            this.maxTextureDownloads = i
            TextureCache.getInstance().setMaxTextureDownloads(this.maxTextureDownloads)
        }
        this.terrainTextures = sharedPreferences.getBoolean("terrain_textures", true)
        i = 64
        try {
            i = Integer.parseInt(sharedPreferences.getString("texture_memory_limit", "64") ?: "64")
        } catch (e: Exception) {
        }
        try {
            this.meshRendering = MeshRendering.valueOf(sharedPreferences.getString("mesh_rendering", "high") ?: "high")
        } catch (e: Exception) {
        }
        TextureMemoryTracker.setMemoryLimit(i * 1024 * 1024)
        this.RLVEnabled = sharedPreferences.getBoolean("rlv_enabled", false)
        this.autoReconnect = sharedPreferences.getBoolean("auto_reconnect", true)
        try {
            this.maxReconnectAttempts = Integer.parseInt(sharedPreferences.getString("reconnect_attempts", "10") ?: "10")
        } catch (e: Exception) {
        }
        updateCacheDir(context, sharedPreferences)
        val hoverText: String = sharedPreferences.getString("hover_text", "huds") ?: "huds"
        if (hoverText == "all") {
            this.hoverTextEnableHUDs = true
            this.hoverTextEnableObjects = true
        } else if (hoverText == "none") {
            this.hoverTextEnableHUDs = false
            this.hoverTextEnableObjects = false
        } else {
            this.hoverTextEnableHUDs = true
            this.hoverTextEnableObjects = false
        }
        this.advancedRendering = sharedPreferences.getBoolean("advanced_rendering", true)
        this.useFXAA = sharedPreferences.getBoolean("fxaa_enable", false)
        this.renderClouds = sharedPreferences.getBoolean("clouds_enable", true)
        val renderTimeOfDay: String = sharedPreferences.getString("render_time_of_day", "sim") ?: "sim"
        if (renderTimeOfDay.equals("sim", ignoreCase = true)) {
            this.forceDaylightTime = false
            this.forceDaylightHour = 0.5f
        } else {
            try {
                this.forceDaylightTime = true
                this.forceDaylightHour = renderTimeOfDay.toFloat()
            } catch (e: Exception) {
                this.forceDaylightTime = false
                this.forceDaylightHour = 0.5f
            }
        }
        if (previousThemeResourceId != this.themeResourceId) {
            EventBus.getInstance().publish(ThemeChangedEvent(this.themeResourceId))
        }
    }
}
