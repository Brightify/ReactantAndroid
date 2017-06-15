package org.brightify.reactant.core.constraint.solver

import org.brightify.reactant.core.constraint.ConstraintOperator
import org.brightify.reactant.core.constraint.ConstraintPriority

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Equation private constructor(val constant: Double, val terms: List<Term>, val operator: ConstraintOperator,
                                            val priority: ConstraintPriority = ConstraintPriority.required) {

    constructor(leftConstant: Number = 0, leftTerms: List<Term> = emptyList(), operator: ConstraintOperator = ConstraintOperator.equal,
                rightConstant: Number = 0, rightTerms: List<Term> = emptyList(), priority: ConstraintPriority = ConstraintPriority.required)
            : this(leftConstant.toDouble() - rightConstant.toDouble(),
            leftTerms + rightTerms.map { Term(-it.coefficient.toDouble(), it.variable) }, operator, priority)
}
