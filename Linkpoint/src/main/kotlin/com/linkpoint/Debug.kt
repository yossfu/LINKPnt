package com.linkpoint

import android.util.Log
import java.nio.ByteBuffer

/**
 * Utilidad de registro (logging) de Ephora Viewer.
 *
 * Reescrito en Kotlin real a partir del Java descompilado: la clase
 * original declaraba `const val` y métodos `@JvmStatic` fuera de un
 * companion object, usaba `objArr.length`, `lastIndexOf(46)` y
 * `catch (Exception e)`, nada de lo cual compila en Kotlin.
 *
 * Semántica preservada: [Printf] y [AlwaysPrintf] escriben en logcat;
 * [Log], [Warning] y [DumpBuffer] son no-op por diseño del original
 * (sus cuerpos estaban vacíos); [isDebugBuild] devuelve siempre false.
 * Todos los llamadores usan estas funciones como estáticas
 * (`Debug.Printf(...)`, `Debug.Warning(e)`, ...), por eso viven en el
 * companion object con @JvmStatic.
 */
class Debug {
    companion object {
        private const val LOG_TAG: String = "Linkpoint"

        @JvmStatic
        fun AlwaysPrintf(str: String, vararg objArr: Any) {
            val stackTraceElement: StackTraceElement = Thread.currentThread().stackTrace[3]
            val className: String = stackTraceElement.className
            Log.d(LOG_TAG, "[" + className.substring(className.lastIndexOf('.') + 1) + "::" + stackTraceElement.methodName + "] " + String.format(str, *objArr))
        }

        @JvmStatic
        fun DumpBuffer(str: String, byteBuffer: ByteBuffer) {
        }

        @JvmStatic
        fun DumpBuffer(str: String, bArr: ByteArray) {
        }

        @JvmStatic
        fun DumpBuffer(str: String, bArr: ByteArray, i: Int) {
        }

        @JvmStatic
        fun Log(str: String) {
        }

        @JvmStatic
        fun Printf(str: String, vararg objArr: Any) {
            try {
                if (objArr.isEmpty()) {
                    Log.d(LOG_TAG, str)
                } else {
                    Log.d(LOG_TAG, String.format(str, *objArr))
                }
            } catch (e: Exception) {
                Log.d(LOG_TAG, str + " (format error: " + e.message + ")")
            }
        }

        @JvmStatic
        fun Warning(th: Throwable) {
        }

        @JvmStatic
        fun isDebugBuild(): Boolean {
            return false
        }
    }
}
