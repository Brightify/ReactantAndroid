package org.brightify.reactant.core.constraint.internal.solver

import org.brightify.reactant.core.constraint.ConstraintVariable

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Term(val coefficient: Double, val variable: ConstraintVariable) {

    constructor(variable: ConstraintVariable) : this(1.0, variable)

    override fun toString(): String {
        val sign = if (coefficient >= 0) "" else "- "
        val coefficient = Math.abs(coefficient)
        val coefficientString = if (coefficient == 1.0) "" else "$coefficient * "
        return "$sign$coefficientString{$variable}"
    }
}
