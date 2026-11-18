package com.linkpoint.render

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * STUB parcial del seguimiento de memoria de texturas.
 *
 * El tipo `com.linkpoint.render.TextureMemoryTracker` no existe en el
 * módulo Linkpoint (la implementación real está en el proyecto `app/`
 * del material original). Se implementan con los contadores y las
 * fórmulas exactas del original solo los métodos que usan los
 * llamadores actuales: `setMemoryLimit` (GlobalOptions),
 * `allocOpenJpegMemory` / `releaseOpenJpegMemory` (OpenJPEG) y
 * `canAllocateMemory` (GLAsyncLoadQueue). Sin coordinación con el
 * renderizador ni estadísticas: es contabilidad, no limitación real.
 */
object TextureMemoryTracker {

    private const val PAGE_SIZE = 4096
    private const val TEXTURE_MAX_RESERVED_MEMORY = 32 * 1024 * 1024

    private val textureMemoryUsed = AtomicInteger(0)
    private val openJpegMemoryUsed = AtomicInteger(0)
    private val openJpegMemoryMmapped = AtomicInteger(0)
    private val textureMemoryLimit = AtomicInteger(64 * 1024 * 1024)
    private val textureMemoryReserved = AtomicInteger(TEXTURE_MAX_RESERVED_MEMORY)
    private val bufMemory = AtomicInteger(0)
    private val stalled = AtomicBoolean(false)

    private fun actualSize(bytes: Int): Int {
        val positive = if (bytes < 0) 0 else bytes
        return ((positive + PAGE_SIZE - 1) / PAGE_SIZE) * PAGE_SIZE
    }

    fun setMemoryLimit(limitBytes: Int) {
        textureMemoryLimit.set(limitBytes)
        textureMemoryReserved.set((limitBytes / 4).coerceAtMost(TEXTURE_MAX_RESERVED_MEMORY))
    }

    fun allocOpenJpegMemory(bytes: Int, memoryMapped: Boolean) {
        val size = actualSize(bytes)
        if (memoryMapped) {
            openJpegMemoryMmapped.addAndGet(size)
        }
        openJpegMemoryUsed.addAndGet(size)
    }

    fun releaseOpenJpegMemory(bytes: Int, memoryMapped: Boolean) {
        val size = actualSize(bytes)
        if (memoryMapped) {
            openJpegMemoryMmapped.addAndGet(-size)
        }
        openJpegMemoryUsed.addAndGet(-size)
    }

    fun canAllocateMemory(bytes: Int): Boolean {
        if (stalled.get()) return false
        val requested = actualSize(bytes)
        val total = textureMemoryUsed.get() + bufMemory.get() + requested + textureMemoryReserved.get()
        return total < textureMemoryLimit.get()
    }
}
