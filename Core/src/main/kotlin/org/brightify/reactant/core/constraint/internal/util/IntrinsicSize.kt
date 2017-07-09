package org.brightify.reactant.core.constraint.internal.util

import android.view.View
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.ConstraintItem
import org.brightify.reactant.core.constraint.internal.ConstraintOperator
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.util.snp
import org.brightify.reactant.core.util.onChange
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class IntrinsicSize(view: View) {

    private val widthVariable = ConstraintVariable(view, ConstraintType.intrinsicWidth)
    private val heightVariable = ConstraintVariable(view, ConstraintType.intrinsicHeight)

    var intrinsicWidth: Double by onChange(0.0) { _, _, _ ->
        horizontalContentHuggingConstraint.isActive = intrinsicWidth != 0.0
        horizontalContentCompressionResistanceConstraint.isActive = intrinsicWidth != 0.0

        view.snp.constraintManager.setValueForVariable(widthVariable, intrinsicWidth)
    }

    var intrinsicHeight: Double by onChange(0.0) { _, _, _ ->
        verticalContentHuggingConstraint.isActive = intrinsicHeight != 0.0
        verticalContentCompressionResistanceConstraint.isActive = intrinsicHeight != 0.0

        view.snp.constraintManager.setValueForVariable(heightVariable, intrinsicHeight)
    }

    var horizontalContentHuggingPriority: ConstraintPriority by Delegates.observable(ConstraintPriority.low) { _, _, _ ->
        horizontalContentHuggingConstraint.priority(horizontalContentHuggingPriority)
    }

    var verticalContentHuggingPriority: ConstraintPriority by Delegates.observable(ConstraintPriority.low) { _, _, _ ->
        verticalContentHuggingConstraint.priority(verticalContentHuggingPriority)
    }

    var horizontalContentCompressionResistancePriority: ConstraintPriority by Delegates.observable(ConstraintPriority.low) { _, _, _ ->
        horizontalContentCompressionResistanceConstraint.priority(horizontalContentCompressionResistancePriority)
    }

    var verticalContentCompressionResistancePriority: ConstraintPriority by Delegates.observable(ConstraintPriority.low) { _, _, _ ->
        verticalContentCompressionResistanceConstraint.priority(verticalContentCompressionResistancePriority)
    }

    private val horizontalContentHuggingConstraint = Constraint(view,
            listOf(ConstraintItem(view.snp.width, ConstraintOperator.lessOrEqual, widthVariable)))
            .priority(horizontalContentHuggingPriority)

    private val verticalContentHuggingConstraint = Constraint(view,
            listOf(ConstraintItem(view.snp.height, ConstraintOperator.lessOrEqual, heightVariable)))
            .priority(verticalContentHuggingPriority)

    private val horizontalContentCompressionResistanceConstraint = Constraint(view,
            listOf(ConstraintItem(view.snp.width, ConstraintOperator.greaterOrEqual, widthVariable)))
            .priority(horizontalContentCompressionResistancePriority)

    private val verticalContentCompressionResistanceConstraint = Constraint(view,
            listOf(ConstraintItem(view.snp.height, ConstraintOperator.greaterOrEqual, heightVariable)))
            .priority(verticalContentCompressionResistancePriority)

    val constraints = listOf(horizontalContentHuggingConstraint, verticalContentHuggingConstraint,
            horizontalContentCompressionResistanceConstraint, verticalContentCompressionResistanceConstraint)

    init {
        constraints.forEach {
            it.isManaged = false
            it.initialized = true
        }
    }
}