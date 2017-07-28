package org.brightify.reactant.core.constraint

import android.view.View
import org.brightify.reactant.core.constraint.exception.InsetUsedOnSizeConstraintException
import org.brightify.reactant.core.constraint.internal.ConstraintItem
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.util.snp

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class Constraint internal constructor(internal val view: View, internal val constraintItems: List<ConstraintItem>) {

    var multiplier: Number
        get() = constraintItems.first().multiplier
        set(value) {
            if (multiplier != value) {
                updateConstraint {
                    constraintItems.forEach { it.multiplier = value }
                }
            }
        }

    var operator: ConstraintOperator
        get() = constraintItems.first().operator
        set(value) {
            if (operator != value) {
                updateConstraint {
                    constraintItems.forEach { it.operator = value }
                }
            }
        }

    var offset: Number
        get() = constraintItems.first().offset
        set(value) {
            if (offset != value) {
                updateConstraint {
                    constraintItems.forEach { it.offset = value }
                }
            }
        }

    var priority: ConstraintPriority
        get() = constraintItems.first().priority
        set(value) {
            if (priority != value) {
                updateConstraint {
                    constraintItems.forEach { it.priority = value }
                }
            }
        }

    var isActive: Boolean
        get() = isManaged
        set(value) {
            if (initialized) {
                if (value) {
                    view.snp.addConstraint(this)
                } else {
                    view.snp.removeConstraint(this)
                }
            }

            isManaged = value
        }

    internal var isManaged = true

    internal var initialized = false

    internal var ignoreInNecessityDecider = false

    fun multipliedBy(value: Number): Constraint {
        this.multiplier = value
        return this
    }

    fun dividedBy(value: Number): Constraint {
        return multipliedBy(1.0 / value.toDouble())
    }

    fun offset(value: Number): Constraint {
        this.offset = value
        return this
    }

    fun priority(value: Int): Constraint {
        return priority(ConstraintPriority(value))
    }

    fun priority(priority: ConstraintPriority): Constraint {
        this.priority = priority
        return this
    }

    fun inset(value: Number): Constraint {
        return inset(value, value, value, value)
    }

    fun inset(top: Number = 0, left: Number = 0, bottom: Number = 0, right: Number = 0): Constraint {
        constraintItems.forEach {
            when (it.type) {
                ConstraintType.top -> it.offset = top
                ConstraintType.left -> it.offset = left
                ConstraintType.bottom -> it.offset = -bottom.toDouble()
                ConstraintType.right -> it.offset = -right.toDouble()
                ConstraintType.centerX -> it.offset = left
                ConstraintType.centerY -> it.offset = top
                else -> throw InsetUsedOnSizeConstraintException()
            }
        }
        return this
    }

    fun activate() {
        isActive = true
    }

    fun deactivate() {
        isActive = false
    }

    override fun toString(): String {
        return constraintItems.map { it.toString() }.joinToString("\n")
    }

    private inline fun updateConstraint(closure: () -> Unit) {
        if (isActive) {
            deactivate()
            closure()
            activate()
        } else {
            closure()
        }
    }
}
