package org.brightify.reactant.autolayout.internal.solver

import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.autolayout.internal.ConstraintType
import kotlin.math.abs
import kotlin.math.sign

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class Term(val coefficient: Double, val variable: ConstraintVariable) {

    val baseTerms: List<Term>
        get() {
            return when (variable.type) {
                ConstraintType.width -> listOf(Term(coefficient, ConstraintVariable(variable.view, ConstraintType.right)),
                        Term(-coefficient, ConstraintVariable(variable.view, ConstraintType.left)))
                ConstraintType.height -> listOf(Term(coefficient, ConstraintVariable(variable.view, ConstraintType.bottom)),
                        Term(-coefficient, ConstraintVariable(variable.view, ConstraintType.top)))
                ConstraintType.centerX -> listOf(Term(
                    sign(coefficient) * (1.0 + (-0.5 * abs(coefficient))),
                        ConstraintVariable(variable.view, ConstraintType.left)),
                        Term(0.5 * coefficient, ConstraintVariable(variable.view, ConstraintType.right)))
                ConstraintType.centerY -> listOf(Term(
                    sign(coefficient) * (1.0 + (-0.5 * abs(coefficient))),
                        ConstraintVariable(variable.view, ConstraintType.top)),
                        Term(0.5 * coefficient, ConstraintVariable(variable.view, ConstraintType.bottom)))
                else -> listOf(Term(coefficient, variable))
            }
        }

    constructor(variable: ConstraintVariable): this(1.0, variable)

    override fun toString(): String {
        val sign = if (coefficient >= 0) "" else "- "
        val coefficient = abs(coefficient)
        val coefficientString = if (coefficient == 1.0) "" else "$coefficient * "
        return "$sign$coefficientString{$variable}"
    }
}
