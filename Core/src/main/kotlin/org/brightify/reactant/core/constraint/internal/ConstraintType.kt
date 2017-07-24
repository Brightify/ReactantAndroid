package org.brightify.reactant.core.constraint.internal

import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.solver.Term

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ConstraintType private constructor(private val description: String) {

    companion object {
        val left = ConstraintType("left")
        val top = ConstraintType("top")
        val right = ConstraintType("right")
        val bottom = ConstraintType("bottom")
        val width = ConstraintType("width")
        val height = ConstraintType("height")
        val centerX = ConstraintType("centerX")
        val centerY = ConstraintType("centerY")

        fun termsForVariable(variable: ConstraintVariable, coefficient: Double): List<Term> {
            return when (variable.type) {
                width -> listOf(Term(coefficient, ConstraintVariable(variable.view, right)),
                        Term(-coefficient, ConstraintVariable(variable.view, left)))
                height -> listOf(Term(coefficient, ConstraintVariable(variable.view, bottom)),
                        Term(-coefficient, ConstraintVariable(variable.view, top)))
                centerX -> listOf(Term(0.5 * coefficient, ConstraintVariable(variable.view, left)),
                        Term(0.5 * coefficient, ConstraintVariable(variable.view, right)))
                centerY -> listOf(Term(0.5 * coefficient, ConstraintVariable(variable.view, top)),
                        Term(0.5 * coefficient, ConstraintVariable(variable.view, bottom)))
                else -> listOf(Term(coefficient, variable))
            }
        }
    }

    override fun toString(): String = description
}
