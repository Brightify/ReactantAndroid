package org.brightify.reactant.core

/**
 * @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
interface ComponentView {

    val isInitialized: Boolean

    val isDestroyed: Boolean

    fun init()

    fun destroy()
}