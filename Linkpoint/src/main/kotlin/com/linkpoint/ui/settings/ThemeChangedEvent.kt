package com.linkpoint.ui.settings

/**
 * Evento publicado en el bus cuando cambia el tema seleccionado.
 *
 * Reescrito en Kotlin real: el original era Java descompilado
 * (`val Int themeResourceId`, constructor Java). Lo publica
 * `GlobalOptions` y lo recibe `ThemedActivity`.
 */
class ThemeChangedEvent(val themeResourceId: Int)
