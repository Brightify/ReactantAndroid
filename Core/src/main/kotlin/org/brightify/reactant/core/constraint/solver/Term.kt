package org.brightify.reactant.core.constraint.solver

import org.brightify.reactant.core.constraint.ConstraintVariable

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Term(val coefficient: Number, val variable: ConstraintVariable) {

    constructor(variable: ConstraintVariable) : this(1, variable)

    override fun toString(): String {
        val sign = if (coefficient.toDouble() >= 0) "" else "- "
        val coefficient = Math.abs(coefficient.toDouble())
        val coefficientString = if (coefficient == 1.0) "" else "$coefficient * "
        return "$sign$coefficientString{$variable}"
    }
}

internal fun Iterable<Term>.simplified(): List<Term> {
    return groupBy { it.variable }
            .map {
                Term(it.value.map { it.coefficient.toDouble() }.reduce { acc, coefficient -> acc + coefficient }, it.key)
            }
            .filter { !it.coefficient.toDouble().isAlmostZero }
}
