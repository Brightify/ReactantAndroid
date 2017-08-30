package org.brightify.reactant.core.component

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
interface MutableComponentState<Self> {

    fun clone(): Self
}
