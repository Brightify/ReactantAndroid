package org.brightify.reactant.autolayout.internal

import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.ConstraintOperator
import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class AutoLayoutConstraints(autoLayout: AutoLayout) {

    var width: Double by onChange(-1.0) { _, _, _ ->
        widthConstraint.offset = width
        widthConstraint.isActive = isActive && width >= 0
    }

    var height: Double by onChange(-1.0) { _, _, _ ->
        heightConstraint.offset = height
        heightConstraint.isActive = isActive && height >= 0
    }

    var widthIsAtMost: Boolean by onChange(false) { _, _, _ ->
        widthConstraint.operator = if (widthIsAtMost) ConstraintOperator.lessOrEqual else ConstraintOperator.equal
    }

    var heightIsAtMost: Boolean by onChange(false) { _, _, _ ->
        heightConstraint.operator = if (heightIsAtMost) ConstraintOperator.lessOrEqual else ConstraintOperator.equal
    }

    private var isActive = false

    private val widthConstraint = Constraint(autoLayout,
            listOf(ConstraintItem(ConstraintVariable(autoLayout, ConstraintType.width), ConstraintOperator.equal)))
    private val heightConstraint = Constraint(autoLayout,
            listOf(ConstraintItem(ConstraintVariable(autoLayout, ConstraintType.height), ConstraintOperator.equal)))
    private val cornerConstraint = Constraint(autoLayout,
            listOf(
                    ConstraintItem(ConstraintVariable(autoLayout, ConstraintType.top), ConstraintOperator.equal),
                    ConstraintItem(ConstraintVariable(autoLayout, ConstraintType.left), ConstraintOperator.equal)
            ))

    init {
        widthConstraint.deactivate()
        heightConstraint.deactivate()
        cornerConstraint.deactivate()
        widthConstraint.initialized = true
        heightConstraint.initialized = true
        cornerConstraint.initialized = true
    }

    fun setActive(isActive: Boolean) {
        widthConstraint.isActive = isActive && width >= 0
        heightConstraint.isActive = isActive && height >= 0
        cornerConstraint.isActive = isActive
        this.isActive = isActive
    }
}