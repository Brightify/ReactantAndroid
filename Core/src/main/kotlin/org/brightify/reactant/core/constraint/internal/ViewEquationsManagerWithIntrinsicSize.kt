package org.brightify.reactant.core.constraint.internal

import android.view.View
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.internal.solver.Equation
import org.brightify.reactant.core.constraint.internal.solver.Solver
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ViewEquationsManagerWithIntrinsicSize(view: View) : ViewEquationsManager(view) {

    //    private val intrinsicWidthVariable = ConstraintVariable(view, ConstraintType.intrinsicWidth)
    //    private val intrinsicHeightVariable = ConstraintVariable(view, ConstraintType.intrinsicHeight)

    var intrinsicWidth: Double by onChange(-1.0) { _, _, _ ->
        //        solver?.removeEquation(intrinsicWidthEquation)
        //        intrinsicWidthEquation = Equation(intrinsicWidthEquation.terms, constant = intrinsicWidth)
        //        solver?.addEquation(intrinsicWidthEquation)
        solver?.removeEquation(intrinsicWidthEquation)
        solver?.removeEquation(horizontalContentHuggingEquation)
        solver?.removeEquation(horizontalContentCompressionResistanceEquation)
        intrinsicWidthEquation = Equation(intrinsicWidthEquation, intrinsicWidth)
        horizontalContentHuggingEquation = Equation(horizontalContentHuggingEquation, intrinsicWidth)
        horizontalContentCompressionResistanceEquation = Equation(horizontalContentCompressionResistanceEquation, intrinsicWidth)
        if (horizontalContentHuggingPriority == horizontalContentCompressionResistancePriority) {
            solver?.addEquation(intrinsicWidthEquation)
        } else {
            solver?.addEquation(horizontalContentHuggingEquation)
            solver?.addEquation(horizontalContentCompressionResistanceEquation)
        }
    }

    var intrinsicHeight: Double by onChange(-1.0) { _, _, _ ->
        //        solver?.removeEquation(intrinsicHeightEquation)
        //        intrinsicHeightEquation = Equation(intrinsicHeightEquation.terms, constant = intrinsicHeight)
        //        solver?.addEquation(intrinsicHeightEquation)
        solver?.removeEquation(intrinsicHeightEquation)
        solver?.removeEquation(verticalContentHuggingEquation)
        solver?.removeEquation(verticalContentCompressionResistanceEquation)
        intrinsicHeightEquation = Equation(intrinsicHeightEquation, intrinsicHeight)
        verticalContentHuggingEquation = Equation(verticalContentHuggingEquation, intrinsicHeight)
        verticalContentCompressionResistanceEquation = Equation(verticalContentCompressionResistanceEquation, intrinsicHeight)
        if (verticalContentHuggingPriority == verticalContentCompressionResistancePriority) {
            solver?.addEquation(intrinsicHeightEquation)
        } else {
            solver?.addEquation(verticalContentHuggingEquation)
            solver?.addEquation(verticalContentCompressionResistanceEquation)
        }
    }

    var horizontalContentHuggingPriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, oldValue, _ ->
        if (oldValue == horizontalContentCompressionResistancePriority) {
            solver?.removeEquation(intrinsicWidthEquation)
        } else {
            solver?.removeEquation(horizontalContentHuggingEquation)
        }
        intrinsicWidthEquation = Equation(intrinsicWidthEquation, horizontalContentHuggingPriority)
        horizontalContentHuggingEquation = Equation(horizontalContentHuggingEquation, horizontalContentHuggingPriority)
        if (horizontalContentHuggingPriority == horizontalContentCompressionResistancePriority) {
            solver?.addEquation(intrinsicWidthEquation)
        } else {
            solver?.addEquation(horizontalContentHuggingEquation)
            if (oldValue == horizontalContentCompressionResistancePriority) {
                solver?.addEquation(horizontalContentCompressionResistanceEquation)
            }
        }
    }

    var verticalContentHuggingPriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, oldValue, _ ->
        if (oldValue == verticalContentCompressionResistancePriority) {
            solver?.removeEquation(intrinsicHeightEquation)
        } else {
            solver?.removeEquation(verticalContentHuggingEquation)
        }
        intrinsicHeightEquation = Equation(intrinsicHeightEquation, verticalContentHuggingPriority)
        verticalContentHuggingEquation = Equation(verticalContentHuggingEquation, verticalContentHuggingPriority)
        if (verticalContentHuggingPriority == verticalContentCompressionResistancePriority) {
            solver?.addEquation(intrinsicHeightEquation)
        } else {
            solver?.addEquation(verticalContentHuggingEquation)
            if (oldValue == verticalContentCompressionResistancePriority) {
                solver?.addEquation(verticalContentCompressionResistanceEquation)
            }
        }
    }

    var horizontalContentCompressionResistancePriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, oldValue, _ ->
        if (oldValue == horizontalContentHuggingPriority) {
            solver?.removeEquation(intrinsicWidthEquation)
        } else {
            solver?.removeEquation(horizontalContentCompressionResistanceEquation)
        }
        intrinsicWidthEquation = Equation(intrinsicWidthEquation, horizontalContentCompressionResistancePriority)
        horizontalContentCompressionResistanceEquation = Equation(horizontalContentCompressionResistanceEquation,
                horizontalContentCompressionResistancePriority)
        if (horizontalContentHuggingPriority == horizontalContentCompressionResistancePriority) {
            solver?.addEquation(intrinsicWidthEquation)
        } else {
            solver?.addEquation(horizontalContentCompressionResistanceEquation)
            if (oldValue == horizontalContentHuggingPriority) {
                solver?.addEquation(horizontalContentHuggingEquation)
            }
        }
    }

    var verticalContentCompressionResistancePriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, oldValue, _ ->
        if (oldValue == verticalContentHuggingPriority) {
            solver?.removeEquation(intrinsicHeightEquation)
        } else {
            solver?.removeEquation(verticalContentCompressionResistanceEquation)
        }
        intrinsicHeightEquation = Equation(intrinsicHeightEquation, verticalContentCompressionResistancePriority)
        verticalContentCompressionResistanceEquation = Equation(verticalContentCompressionResistanceEquation,
                verticalContentCompressionResistancePriority)
        if (verticalContentHuggingPriority == verticalContentCompressionResistancePriority) {
            solver?.addEquation(intrinsicHeightEquation)
        } else {
            solver?.addEquation(verticalContentCompressionResistanceEquation)
            if (oldValue == verticalContentHuggingPriority) {
                solver?.addEquation(verticalContentHuggingEquation)
            }
        }
    }

        private var intrinsicWidthEquation = Equation(ConstraintType.termsForVariable(widthVariable, 1.0),
                operator = ConstraintOperator.equal, priority = ConstraintPriority.low)

        private var intrinsicHeightEquation = Equation(ConstraintType.termsForVariable(heightVariable, 1.0),
                operator = ConstraintOperator.equal, priority = ConstraintPriority.low)

    private var horizontalContentHuggingEquation = Equation(ConstraintType.termsForVariable(widthVariable, 1.0),
            operator = ConstraintOperator.lessOrEqual, priority = ConstraintPriority.low)

    private var verticalContentHuggingEquation = Equation(ConstraintType.termsForVariable(heightVariable, 1.0),
            operator = ConstraintOperator.lessOrEqual, priority = ConstraintPriority.low)

    private var horizontalContentCompressionResistanceEquation = Equation(ConstraintType.termsForVariable(widthVariable, 1.0),
            operator = ConstraintOperator.greaterOrEqual, priority = ConstraintPriority.low)

    private var verticalContentCompressionResistanceEquation = Equation(ConstraintType.termsForVariable(heightVariable, 1.0),
            operator = ConstraintOperator.greaterOrEqual, priority = ConstraintPriority.low)

    override fun addEquations(solver: Solver) {
        super.addEquations(solver)

        //        solver.addEquation(intrinsicWidthEquation)
        //        solver.addEquation(intrinsicHeightEquation)
//        solver.addEquation(horizontalContentHuggingEquation)
//        solver.addEquation(verticalContentHuggingEquation)
//        solver.addEquation(horizontalContentCompressionResistanceEquation)
//        solver.addEquation(verticalContentCompressionResistanceEquation)
    }

    override fun removeEquations() {
        super.removeEquations()

        //        solver?.removeEquation(intrinsicWidthEquation)
        //        solver?.removeEquation(intrinsicHeightEquation)
        solver?.removeEquation(intrinsicWidthEquation)
        solver?.removeEquation(intrinsicHeightEquation)
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
