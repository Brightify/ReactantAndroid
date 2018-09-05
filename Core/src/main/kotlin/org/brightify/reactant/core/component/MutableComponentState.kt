package org.brightify.reactant.core.component

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
interface MutableComponentState<Self> {

    fun clone(): Self
}
