package org.brightify.reactant.core.constraint.internal.view

import android.view.View
import org.brightify.reactant.core.constraint.CollapseAxis
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintOperator
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.ConstraintItem
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class VisibilityManager(view: View) {

    var collapseAxis = CollapseAxis.vertical

    var visibility: Int by onChange(view.visibility) { _, _, _ ->
        if (visibility == View.GONE) {
            when (collapseAxis) {
                CollapseAxis.horizontal -> widthConstraint.activate()
                CollapseAxis.vertical -> heightConstraint.activate()
                CollapseAxis.both -> {
                    widthConstraint.activate()
                    heightConstraint.activate()
                }
            }
        } else {
            widthConstraint.deactivate()
            heightConstraint.deactivate()
        }
    }

    private val widthConstraint = Constraint(view,
            listOf(ConstraintItem(ConstraintVariable(view, ConstraintType.width), ConstraintOperator.equal)))

    private val heightConstraint = Constraint(view,
            listOf(ConstraintItem(ConstraintVariable(view, ConstraintType.height), ConstraintOperator.equal)))

    init {
        widthConstraint.deactivate()
        heightConstraint.deactivate()
        widthConstraint.initialized = true
        heightConstraint.initialized = true
    }
}