package org.brightify.reactant.core.constraint.util

import org.brightify.reactant.core.constraint.ConstraintType
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.solver.Equation
import org.brightify.reactant.core.constraint.solver.Term

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ComplexEquationsProvider(val viewId: Int) {

    private val width = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.width))),
            rightTerms = listOf(Term(Variable(ConstraintType.right)), Term(-1.0, Variable(ConstraintType.left)))
    )

    private val height = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.height))),
            rightTerms = listOf(Term(Variable(ConstraintType.bottom)), Term(-1.0, Variable(ConstraintType.top)))
    )

    private val centerX = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.centerX))),
            rightTerms = listOf(Term(Variable(ConstraintType.left)), Term(0.5, Variable(ConstraintType.width)))
    )

    private val centerY = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.centerY))),
            rightTerms = listOf(Term(Variable(ConstraintType.top)), Term(0.5, Variable(ConstraintType.height)))
    )

    val equations: List<Equation> = listOf(width, height, centerX, centerY)

    private fun Variable(type: ConstraintType) = ConstraintVariable(viewId, type)
}