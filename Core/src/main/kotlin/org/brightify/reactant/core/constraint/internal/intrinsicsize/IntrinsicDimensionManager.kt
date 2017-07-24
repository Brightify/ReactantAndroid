package org.brightify.reactant.core.constraint.internal.intrinsicsize

import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.ConstraintOperator
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.internal.solver.Equation
import org.brightify.reactant.core.constraint.internal.solver.Solver
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class IntrinsicDimensionManager(var solver: Solver, dimensionVariable: ConstraintVariable) {

    var size: Double by onChange(-1.0) { _, _, _ ->
        updateEquations()
    }

    var contentHuggingPriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        updateEquations()
    }

    var contentCompressionResistancePriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        updateEquations()
    }

    private val terms = ConstraintType.termsForVariable(dimensionVariable, 1.0)

    private var equalEquation: Equation? = null
    private var contentHuggingEquation: Equation? = null
    private var contentCompressionResistanceEquation: Equation? = null

    fun addEquations() {
        equalEquation?.let { solver.addEquation(it) }
        contentHuggingEquation?.let { solver.addEquation(it) }
        contentCompressionResistanceEquation?.let { solver.addEquation(it) }
    }

    fun removeEquations() {
        equalEquation?.let { solver.removeEquation(it) }
        contentHuggingEquation?.let { solver.removeEquation(it) }
        contentCompressionResistanceEquation?.let { solver.removeEquation(it) }
    }

    private fun updateEquations() {
        removeEquations()
        equalEquation = null
        contentHuggingEquation = null
        contentCompressionResistanceEquation = null
        if (size >= 0) {
            if (contentHuggingPriority == contentCompressionResistancePriority) {
                equalEquation = Equation(terms, ConstraintOperator.equal, size, contentHuggingPriority)
            } else {
                contentHuggingEquation = Equation(terms, ConstraintOperator.lessOrEqual, size, contentHuggingPriority)
                contentCompressionResistanceEquation = Equation(terms, ConstraintOperator.greaterOrEqual, size,
                        contentCompressionResistancePriority)
            }
            addEquations()
        }
    }
}