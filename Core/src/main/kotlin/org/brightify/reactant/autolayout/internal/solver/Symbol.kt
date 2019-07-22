package org.brightify.reactant.autolayout.internal.solver

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class Symbol(val type: Type) {

    enum class Type {
        external,
        slack,
        objective,
        dummy
    }
}
