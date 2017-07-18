package org.brightify.reactant.core.constraint.internal.solver

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Symbol(val type: Symbol.Type) {

    enum class Type {
        external,
        slack,
        objective,
        dummy
    }
}
