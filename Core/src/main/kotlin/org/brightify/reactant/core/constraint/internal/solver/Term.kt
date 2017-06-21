package org.brightify.reactant.core.constraint.internal.solver

import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.util.isAlmostZero

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

internal fun Iterable<Term>.simplified(): List<Term> {
    return groupBy { it.variable }
            .map {
                Term(it.value.map { it.coefficient }.reduce { acc, coefficient -> acc + coefficient }, it.key)
            }
            .filter { !it.coefficient.isAlmostZero }
}
