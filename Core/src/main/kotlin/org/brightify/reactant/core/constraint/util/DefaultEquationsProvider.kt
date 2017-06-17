package org.brightify.reactant.core.constraint.util

import android.view.View
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintType
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.solver.Equation
import org.brightify.reactant.core.constraint.solver.Term

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class DefaultEquationsProvider(val view: View) {

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
            rightTerms = listOf(Term(0.5, Variable(ConstraintType.left)), Term(0.5, Variable(ConstraintType.right)))
    )

    private val centerY = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.centerY))),
            rightTerms = listOf(Term(0.5, Variable(ConstraintType.top)), Term(0.5, Variable(ConstraintType.bottom)))
    )

    private val leftMargin = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.leftMargin))),
            rightTerms = listOf(Term(Variable(ConstraintType.left)), Term(-1, Variable(ConstraintType.leftMarginSize)))
    )

    private val topMargin = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.topMargin))),
            rightTerms = listOf(Term(Variable(ConstraintType.top)), Term(-1, Variable(ConstraintType.topMarginSize)))
    )

    private val rightMargin = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.rightMargin))),
            rightTerms = listOf(Term(Variable(ConstraintType.right)), Term(Variable(ConstraintType.rightMarginSize)))
    )

    private val bottomMargin = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.bottomMargin))),
            rightTerms = listOf(Term(Variable(ConstraintType.bottom)), Term(Variable(ConstraintType.bottomMarginSize)))
    )

    private val centerXWithMargins = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.centerXWithMargins))),
            rightTerms = listOf(Term(0.5, Variable(ConstraintType.leftMargin)), Term(0.5, Variable(ConstraintType.rightMargin)))
    )

    private val centerYWithMargins = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.centerYWithMargins))),
            rightTerms = listOf(Term(0.5, Variable(ConstraintType.top)), Term(0.5, Variable(ConstraintType.bottom)))
    )

    private val intrinsicWidth = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.intrinsicWidth))),
            rightTerms = listOf(Term(Variable(ConstraintType.width))),
            priority = ConstraintPriority.low
    )

    private val intrinsicHeight = Equation(
            leftTerms = listOf(Term(Variable(ConstraintType.intrinsicHeight))),
            rightTerms = listOf(Term(Variable(ConstraintType.height))),
            priority = ConstraintPriority.low
    )

    val equations: List<Equation> = listOf(width, height, centerX, centerY, leftMargin, topMargin, rightMargin, bottomMargin,
            centerXWithMargins, centerYWithMargins, intrinsicWidth, intrinsicHeight)

    private fun Variable(type: ConstraintType) = ConstraintVariable(view, type)
}
