package org.brightify.reactant.core.constraint.internal.util

import android.view.View
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.internal.solver.Equation
import org.brightify.reactant.core.constraint.internal.solver.Term

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class DefaultEquationsProvider(val view: View) {

    private val width = Equation(terms = listOf(
            Term(Variable(ConstraintType.width)),
            Term(-1.0, Variable(ConstraintType.right)),
            Term(Variable(ConstraintType.left))
    ))

    private val height = Equation(terms = listOf(
            Term(Variable(ConstraintType.height)),
            Term(-1.0, Variable(ConstraintType.bottom)),
            Term(Variable(ConstraintType.top))
    ))

    private val centerX = Equation(terms = listOf(
            Term(Variable(ConstraintType.centerX)),
            Term(-0.5, Variable(ConstraintType.left)),
            Term(-0.5, Variable(ConstraintType.right))
    ))

    private val centerY = Equation(terms = listOf(
            Term(Variable(ConstraintType.centerY)),
            Term(-0.5, Variable(ConstraintType.top)),
            Term(-0.5, Variable(ConstraintType.bottom))
    ))

    private val leftMargin = Equation(terms = listOf(
            Term(Variable(ConstraintType.leftMargin)),
            Term(-1.0, Variable(ConstraintType.left)),
            Term(Variable(ConstraintType.leftMarginSize))
    ))

    private val topMargin = Equation(terms = listOf(
            Term(Variable(ConstraintType.topMargin)),
            Term(-1.0, Variable(ConstraintType.top)),
            Term(Variable(ConstraintType.topMarginSize))
    ))

    private val rightMargin = Equation(terms = listOf(
            Term(Variable(ConstraintType.rightMargin)),
            Term(-1.0, Variable(ConstraintType.right)),
            Term(-1.0, Variable(ConstraintType.rightMarginSize))
    ))

    private val bottomMargin = Equation(terms = listOf(
            Term(Variable(ConstraintType.bottomMargin)),
            Term(-1.0, Variable(ConstraintType.bottom)),
            Term(-1.0, Variable(ConstraintType.bottomMarginSize))
    ))

    private val centerXWithMargins = Equation(terms = listOf(
            Term(Variable(ConstraintType.centerXWithMargins)),
            Term(-0.5, Variable(ConstraintType.leftMargin)),
            Term(-0.5, Variable(ConstraintType.rightMargin))
    ))

    private val centerYWithMargins = Equation(terms = listOf(
            Term(Variable(ConstraintType.centerYWithMargins)),
            Term(-0.5, Variable(ConstraintType.top)),
            Term(-0.5, Variable(ConstraintType.bottom))
    ))

    val equations = listOf(width, height, centerX, centerY, leftMargin, topMargin, rightMargin, bottomMargin,
            centerXWithMargins, centerYWithMargins)

    private fun Variable(type: ConstraintType) = ConstraintVariable(view, type)
}
