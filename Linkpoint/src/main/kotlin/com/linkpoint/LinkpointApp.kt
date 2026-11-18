package com.linkpoint

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.AssetManager
import android.preference.PreferenceManager
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import com.linkpoint.debug.AutoLogUploader
import com.linkpoint.fixes.ResourceConflictResolver
import com.linkpoint.ui.login.CleanLoginActivity
import kotlin.math.sqrt

/**
 * Main Application class for Linkpoint Viewer.
 *
 * Handles global application state, resource conflict resolution, and system-wide initialization.
 * Updated to use AndroidX libraries and modern Android development practices.
 * Extends MultiDexApplication to support large applications with 64K+ methods.
 *
 * Hito de login (2026-11-18): los componentes modernos (ModernLinkpointDemo,
 * 639 líneas de Java descompilado, y sus 6 dependencias, ~2.800 líneas) NO se
 * instancian: la app arranca en modo seguro y abre la pantalla de login.
 * Se reintroducirán de verdad en fases posteriores, no como demo simulada.
 */
class LinkpointApp : MultiDexApplication() {

    companion object {
        private const val TAG: String = "LinkpointApp"

        private val displayMetrics = DisplayMetrics()

        private lateinit var mContext: Context

        private val prefs: SharedPreferences by lazy {
            PreferenceManager.getDefaultSharedPreferences(mContext)
        }

        private fun isContextInitialized(): Boolean = ::mContext.isInitialized

        @JvmStatic
        fun getAppVersion(): String {
            return try {
                mContext.packageManager.getPackageInfo(mContext.packageName, 0)?.versionName ?: ""
            } catch (e: NameNotFoundException) {
                Log.w(TAG, "Could not get app version", e)
                ""
            }
        }

        /**
         * Get application startup status and any initialization errors
         */
        @JvmStatic
        fun getStartupStatus(): String {
            if (!isContextInitialized()) {
                return "Application context not initialized"
            }

            val status: StringBuilder = StringBuilder()
            status.append("Linkpoint Application Status:\n")
            status.append("- Context: OK\n")
            status.append("- Modern Components: Safe Mode (desactivados en el hito de login)\n")

            return status.toString()
        }

        /**
         * Upload debug logs immediately (for debug builds only)
         */
        @JvmStatic
        fun uploadDebugLogsNow(reason: String) {
            if (isContextInitialized()) {
                try {
                    AutoLogUploader.getInstance(mContext).uploadLogsNow(reason)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to trigger log upload", e)
                }
            }
        }

        /**
         * Report a crash for automatic upload (debug builds only)
         */
        @JvmStatic
        fun reportCrash(crash: Throwable, additionalInfo: String) {
            if (isContextInitialized()) {
                try {
                    AutoLogUploader.getInstance(mContext).uploadCrashReport(crash, additionalInfo)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload crash report", e)
                }
            }
        }

        @JvmStatic
        fun getAssetManager(): AssetManager {
            return mContext.assets
        }

        @JvmStatic
        fun getContext(): Context {
            return mContext
        }

        @JvmStatic
        fun getDefaultSharedPreferences(): SharedPreferences {
            return prefs
        }

        @JvmStatic
        fun isSplitScreenNeeded(context: Context): Boolean {
            val string: String = getDefaultSharedPreferences().getString("split_screens", "auto") ?: "auto"
            when (string) {
                "never" -> return false
                "always" -> return true
                "landscape" -> {
                    val defaultDisplay: Display =
                        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                    return defaultDisplay.width > defaultDisplay.height
                }
                else -> {
                    val defaultDisplay: Display =
                        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                    defaultDisplay.getMetrics(displayMetrics)
                    val f: Float = displayMetrics.heightPixels.toFloat() / displayMetrics.ydpi
                    val f2: Float = displayMetrics.widthPixels.toFloat() / displayMetrics.xdpi
                    val diagonalInches: Double = sqrt(((f * f) + (f2 * f2)).toDouble())
                    if (diagonalInches <= 6.5 || f2 < 5.0f) {
                        return false
                    }
                    Log.i(
                        TAG, String.format(
                            "LinkpointApp: Display width in dp: %.2f, xInches %.1f, diag %.1f",
                            defaultDisplay.width.toFloat() / displayMetrics.density, f2, diagonalInches
                        )
                    )
                    return defaultDisplay.width.toFloat() / displayMetrics.density >= 1000.0f
                }
            }
        }

        @JvmStatic
        fun restartApp() {
            try {
                val alarmManager: AlarmManager =
                    getContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                // No existe ninguna clase LauncherActivity en el árbol; el reinicio
                // va a CleanLoginActivity, la actividad LAUNCHER declarada en el manifest.
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    getContext(), 0,
                    Intent(getContext(), CleanLoginActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent)
                System.exit(0)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restart app", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Linkpoint Application starting up")

        try {
            // Set global context first - this is critical for all other operations
            mContext = this
            Log.i(TAG, "Global context set successfully")

            // CRITICAL: Initialize resource conflict resolver before any other initialization
            // This fixes build-breaking resource conflicts between AndroidX and support libraries
            try {
                ResourceConflictResolver.initialize(this)
                Log.i(TAG, "Resource conflict resolver initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Resource conflict resolver failed - continuing anyway", e)
            }

            // Initialize global options after resource conflicts are resolved
            // GlobalOptions.getInstance().initialize();  // Fuera del cierre de
            // login: GlobalOptions arrastra cientos de archivos rotos; no se
            // usa en la pantalla de login.

            // Componentes modernos desactivados en el hito de login (ver
            // comentario en la cabecera de la clase).
            Log.w(TAG, "Modern components disabled for the login milestone - Safe Mode")

            // Initialize automatic log upload for debug builds
            initializeDebugLogUpload()

            Log.i(TAG, "Linkpoint Application initialization complete")
        } catch (e: Throwable) {
            // Catch all throwables including OutOfMemoryError, LinkageError, etc.
            Log.e(TAG, "CRITICAL: Application initialization failed", e)

            // Upload crash report for debug builds
            try {
                AutoLogUploader.getInstance(this).uploadCrashReport(e, "Application initialization failure")
            } catch (uploadError: Exception) {
                Log.e(TAG, "Failed to upload crash report", uploadError)
            }

            // Log specific error types for debugging
            if (e is OutOfMemoryError) {
                Log.e(TAG, "Out of memory during initialization")
            } else if (e is NoClassDefFoundError) {
                Log.e(TAG, "Missing class definition: " + e.message)
            } else if (e is UnsatisfiedLinkError) {
                Log.e(TAG, "Native library linking failed: " + e.message)
            }

            Log.w(TAG, "Application will continue in SAFE MODE with basic functionality only")

            // Don't re-throw - allow the app to continue running
        }
    }

    /**
     * Initialize debug log upload system for debug builds only
     */
    private fun initializeDebugLogUpload() {
        try {
            val logUploader: AutoLogUploader = AutoLogUploader.getInstance(this)
            logUploader.initializeAutoUpload()
            Log.i(TAG, "Debug log upload system initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize debug log upload", e)
            // Don't crash the app if log upload fails
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
