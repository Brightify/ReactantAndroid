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
        horizontalContentHuggingConstraint.deactivate()
        horizontalContentCompressionResistanceConstraint.deactivate()

        horizontalContentHuggingConstraint.offset(intrinsicWidth)
        horizontalContentCompressionResistanceConstraint.offset(intrinsicWidth)

        if (intrinsicWidth != 0.0) {
            horizontalContentHuggingConstraint.activate()
            horizontalContentCompressionResistanceConstraint.activate()
        }
    }

    var intrinsicHeight: Double by Delegates.observable(0.0) { _, _, _ ->
        verticalContentHuggingConstraint.deactivate()
        verticalContentCompressionResistanceConstraint.deactivate()

        verticalContentHuggingConstraint.offset(intrinsicHeight)
        verticalContentCompressionResistanceConstraint.offset(intrinsicHeight)

        if (intrinsicHeight != 0.0) {
            verticalContentHuggingConstraint.activate()
            verticalContentCompressionResistanceConstraint.activate()
        }
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