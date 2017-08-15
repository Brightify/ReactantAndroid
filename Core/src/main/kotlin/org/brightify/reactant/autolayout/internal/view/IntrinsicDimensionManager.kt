package org.brightify.reactant.autolayout.internal.view

import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.ConstraintOperator
import org.brightify.reactant.autolayout.ConstraintPriority
import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.autolayout.internal.ConstraintItem
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class IntrinsicDimensionManager(dimensionVariable: ConstraintVariable) {

    var size: Double by onChange(-1.0) { _, _, _ ->
        updateEquations()
    }

    var contentHuggingPriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        updateEquations()
    }

    var contentCompressionResistancePriority: ConstraintPriority by onChange(ConstraintPriority.low) { _, _, _ ->
        updateEquations()
    }

    val isUsingEqualConstraint: Boolean
        get() = contentHuggingPriority == contentCompressionResistancePriority

    val equalConstraintForNecessityDecider: Constraint
        get() = equalConstraint.priority(contentHuggingPriority)

    private val equalConstraint = Constraint(dimensionVariable.view,
            listOf(ConstraintItem(dimensionVariable, ConstraintOperator.equal)))
    private val contentHuggingConstraint = Constraint(dimensionVariable.view,
            listOf(ConstraintItem(dimensionVariable, ConstraintOperator.lessOrEqual)))
    private val contentCompressionResistanceConstraint = Constraint(dimensionVariable.view,
            listOf(ConstraintItem(dimensionVariable, ConstraintOperator.greaterOrEqual)))

    init {
        deactivateConstraints()

        equalConstraint.initialized = true
        contentHuggingConstraint.initialized = true
        contentCompressionResistanceConstraint.initialized = true
    }

    private fun updateEquations() {
        deactivateConstraints()
        if (size >= 0) {
            if (isUsingEqualConstraint) {
                equalConstraint.offset = size
                equalConstraint.priority = contentHuggingPriority
                equalConstraint.activate()
            } else {
                contentHuggingConstraint.offset = size
                contentCompressionResistanceConstraint.offset = size
                contentHuggingConstraint.priority = contentHuggingPriority
                contentCompressionResistanceConstraint.priority = contentCompressionResistancePriority
                contentHuggingConstraint.activate()
                contentCompressionResistanceConstraint.activate()
            }
        }
    }

    private fun deactivateConstraints() {
        equalConstraint.deactivate()
        contentHuggingConstraint.deactivate()
        contentCompressionResistanceConstraint.deactivate()
    }
}