package org.brightify.reactant.core.constraint.internal

import android.view.View
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.internal.solver.Equation
import org.brightify.reactant.core.constraint.internal.solver.Solver
import org.brightify.reactant.core.constraint.internal.solver.Term
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ViewEquationsManagerWithIntrinsicSize(view: View) : ViewEquationsManager(view) {

    //    private val intrinsicWidthVariable = ConstraintVariable(view, ConstraintType.intrinsicWidth)
    //    private val intrinsicHeightVariable = ConstraintVariable(view, ConstraintType.intrinsicHeight)

    var intrinsicWidth: Double by onChange(0.0) { _, _, _ ->
        //        solver?.removeEquation(intrinsicWidthEquation)
        //        intrinsicWidthEquation = Equation(intrinsicWidthEquation.terms, constant = intrinsicWidth)
        //        solver?.addEquation(intrinsicWidthEquation)
        solver?.removeEquation(horizontalContentHuggingEquation)
        solver?.removeEquation(horizontalContentCompressionResistanceEquation)
        horizontalContentHuggingEquation = Equation(horizontalContentHuggingEquation, intrinsicWidth)
        horizontalContentCompressionResistanceEquation = Equation(horizontalContentCompressionResistanceEquation, intrinsicWidth)
        solver?.addEquation(horizontalContentHuggingEquation)
        solver?.addEquation(horizontalContentCompressionResistanceEquation)
    }

    var intrinsicHeight: Double by onChange(0.0) { _, _, _ ->
        //        solver?.removeEquation(intrinsicHeightEquation)
        //        intrinsicHeightEquation = Equation(intrinsicHeightEquation.terms, constant = intrinsicHeight)
        //        solver?.addEquation(intrinsicHeightEquation)
        solver?.removeEquation(verticalContentHuggingEquation)
        solver?.removeEquation(verticalContentCompressionResistanceEquation)
        verticalContentHuggingEquation = Equation(verticalContentHuggingEquation, intrinsicHeight)
        verticalContentCompressionResistanceEquation = Equation(verticalContentCompressionResistanceEquation, intrinsicHeight)
        solver?.addEquation(verticalContentHuggingEquation)
        solver?.addEquation(verticalContentCompressionResistanceEquation)
    }

    var horizontalContentHuggingPriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        solver?.removeEquation(horizontalContentHuggingEquation)
        horizontalContentHuggingEquation = Equation(horizontalContentHuggingEquation, horizontalContentHuggingPriority)
        solver?.addEquation(horizontalContentHuggingEquation)
    }

    var verticalContentHuggingPriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        solver?.removeEquation(verticalContentHuggingEquation)
        verticalContentHuggingEquation = Equation(verticalContentHuggingEquation, verticalContentHuggingPriority)
        solver?.addEquation(verticalContentHuggingEquation)
    }

    var horizontalContentCompressionResistancePriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        solver?.removeEquation(horizontalContentCompressionResistanceEquation)
        horizontalContentCompressionResistanceEquation = Equation(horizontalContentCompressionResistanceEquation,
                horizontalContentCompressionResistancePriority)
        solver?.addEquation(horizontalContentCompressionResistanceEquation)
    }

    var verticalContentCompressionResistancePriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        solver?.removeEquation(verticalContentCompressionResistanceEquation)
        verticalContentCompressionResistanceEquation = Equation(verticalContentCompressionResistanceEquation,
                verticalContentCompressionResistancePriority)
        solver?.addEquation(verticalContentCompressionResistanceEquation)
    }

    //    private var intrinsicWidthEquation = Equation(listOf(Term(intrinsicWidthVariable)))
    //
    //    private var intrinsicHeightEquation = Equation(listOf(Term(intrinsicHeightVariable)))

    private var horizontalContentHuggingEquation = Equation(listOf(Term(widthVariable)),
            operator = ConstraintOperator.lessOrEqual, priority = ConstraintPriority.low)

    private var verticalContentHuggingEquation = Equation(listOf(Term(heightVariable)),
            operator = ConstraintOperator.lessOrEqual, priority = ConstraintPriority.low)

    private var horizontalContentCompressionResistanceEquation = Equation(listOf(Term(widthVariable)),
            operator = ConstraintOperator.greaterOrEqual, priority = ConstraintPriority.low)

    private var verticalContentCompressionResistanceEquation = Equation(listOf(Term(heightVariable)),
            operator = ConstraintOperator.greaterOrEqual, priority = ConstraintPriority.low)

    override fun addEquations(solver: Solver) {
        super.addEquations(solver)

        //        solver.addEquation(intrinsicWidthEquation)
        //        solver.addEquation(intrinsicHeightEquation)
        solver.addEquation(horizontalContentHuggingEquation)
        solver.addEquation(verticalContentHuggingEquation)
        solver.addEquation(horizontalContentCompressionResistanceEquation)
        solver.addEquation(verticalContentCompressionResistanceEquation)
    }

    override fun removeEquations() {
        super.removeEquations()

        //        solver?.removeEquation(intrinsicWidthEquation)
        //        solver?.removeEquation(intrinsicHeightEquation)
        solver?.removeEquation(horizontalContentHuggingEquation)
        solver?.removeEquation(verticalContentHuggingEquation)
        solver?.removeEquation(horizontalContentCompressionResistanceEquation)
        solver?.removeEquation(verticalContentCompressionResistanceEquation)

        solver = null
    }

    private fun Equation(equation: Equation, priority: ConstraintPriority): Equation {
        return Equation(equation.terms, equation.operator, equation.constant, priority)
    }

    private fun Equation(equation: Equation, constant: Double): Equation {
        return Equation(equation.terms, equation.operator, constant, equation.priority)
    }
}
