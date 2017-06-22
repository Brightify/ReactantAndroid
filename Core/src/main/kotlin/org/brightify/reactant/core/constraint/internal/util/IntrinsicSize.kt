package org.brightify.reactant.core.constraint.internal.util

import android.view.View
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.internal.ConstraintItem
import org.brightify.reactant.core.constraint.internal.ConstraintOperator
import org.brightify.reactant.core.constraint.util.snp
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class IntrinsicSize(view: View) {

    var intrinsicWidth: Double by Delegates.observable(0.0) { _, _, _ ->
        if (intrinsicWidth == 0.0) {
            horizontalContentHuggingConstraint.deactivate()
            horizontalContentCompressionResistanceConstraint.deactivate()
        } else {
            horizontalContentHuggingConstraint.activate()
            horizontalContentCompressionResistanceConstraint.activate()
        }

        horizontalContentHuggingConstraint.offset(intrinsicWidth)
        horizontalContentCompressionResistanceConstraint.offset(intrinsicWidth)
    }

    var intrinsicHeight: Double by Delegates.observable(0.0) { _, _, _ ->
        if (intrinsicHeight == 0.0) {
            verticalContentHuggingConstraint.deactivate()
            verticalContentCompressionResistanceConstraint.deactivate()
        } else {
            verticalContentHuggingConstraint.activate()
            verticalContentCompressionResistanceConstraint.activate()
        }

        verticalContentHuggingConstraint.offset(intrinsicHeight)
        verticalContentCompressionResistanceConstraint.offset(intrinsicHeight)
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
            listOf(ConstraintItem(view.snp.width, ConstraintOperator.lessOrEqual, offset = intrinsicWidth)))
            .priority(horizontalContentHuggingPriority)

    private val verticalContentHuggingConstraint = Constraint(view,
            listOf(ConstraintItem(view.snp.height, ConstraintOperator.lessOrEqual, offset = intrinsicHeight)))
            .priority(verticalContentHuggingPriority)

    private val horizontalContentCompressionResistanceConstraint = Constraint(view,
            listOf(ConstraintItem(view.snp.width, ConstraintOperator.greaterOrEqual, offset = intrinsicWidth)))
            .priority(horizontalContentCompressionResistancePriority)

    private val verticalContentCompressionResistanceConstraint = Constraint(view,
            listOf(ConstraintItem(view.snp.height, ConstraintOperator.greaterOrEqual, offset = intrinsicHeight)))
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