package org.brightify.reactant.autolayout.internal.solver

import org.brightify.reactant.autolayout.ConstraintOperator
import org.brightify.reactant.autolayout.ConstraintPriority

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class Equation(val terms: List<Term> = emptyList(),
                        val operator: ConstraintOperator = ConstraintOperator.equal,
                        val constant: Double = 0.0,
                        val priority: ConstraintPriority = ConstraintPriority.required)