package org.brightify.reactant.core.constraint.internal

import android.view.View
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.solver.Equation
import org.brightify.reactant.core.constraint.internal.solver.Solver
import org.brightify.reactant.core.constraint.internal.solver.Term

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal open class ViewEquationsManager(val view: View) {

    var solver: Solver? = null

    // TODO Used cached
    protected val widthVariable = ConstraintVariable(view, ConstraintType.width)
    protected val heightVariable = ConstraintVariable(view, ConstraintType.height)

    private val topVariable = ConstraintVariable(view, ConstraintType.top)
    private val leftVariable = ConstraintVariable(view, ConstraintType.left)
    private val bottomVariable = ConstraintVariable(view, ConstraintType.bottom)
    private val rightVariable = ConstraintVariable(view, ConstraintType.right)
    private val centerXVariable = ConstraintVariable(view, ConstraintType.centerX)
    private val centerYVariable = ConstraintVariable(view, ConstraintType.centerY)

    private val width = Equation(terms = listOf(
            Term(widthVariable),
            Term(-1.0, rightVariable),
            Term(leftVariable)
    ))

    private val height = Equation(terms = listOf(
            Term(heightVariable),
            Term(-1.0, bottomVariable),
            Term(topVariable)
    ))

    private val centerX = Equation(terms = listOf(
            Term(centerXVariable),
            Term(-0.5, leftVariable),
            Term(-0.5, rightVariable)
    ))

    private val centerY = Equation(terms = listOf(
            Term(centerYVariable),
            Term(-0.5, topVariable),
            Term(-0.5, bottomVariable)
    ))

    open fun addEquations(solver: Solver) {
        this.solver = solver
//        solver.addEquation(width)
//        solver.addEquation(height)
//        solver.addEquation(centerX)
//        solver.addEquation(centerY)
    }

    open fun removeEquations() {
//        solver?.removeEquation(width)
//        solver?.removeEquation(height)
//        solver?.removeEquation(centerX)
//        solver?.removeEquation(centerY)
    }
}
