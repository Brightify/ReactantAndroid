package org.brightify.reactant.core.constraint.internal.solver

import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintOperator

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Equation(val terms: List<Term> = emptyList(),
                        val operator: ConstraintOperator = ConstraintOperator.equal,
                        val constant: Double = 0.0,
                        val priority: ConstraintPriority = ConstraintPriority.required) {

    override fun toString(): String {
        val termsString = terms.map { it.toString() }.joinToString(" ")
        return "$termsString $operator $constant ($priority)"
    }
}