package org.brightify.reactant.core.constraint.internal.solver

import org.brightify.reactant.core.constraint.ConstraintOperator
import org.brightify.reactant.core.constraint.ConstraintPriority

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Equation(val terms: List<Term> = emptyList(),
                        val operator: ConstraintOperator = ConstraintOperator.equal,
                        val constant: Double = 0.0,
                        val priority: ConstraintPriority = ConstraintPriority.required) {
}