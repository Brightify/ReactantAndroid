package org.brightify.reactant.core.constraint.dsl

import android.view.View
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.exception.MissingParentException
import org.brightify.reactant.core.constraint.internal.ConstraintItem
import org.brightify.reactant.core.constraint.internal.ConstraintOperator
import org.brightify.reactant.core.constraint.internal.ConstraintType

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintMaker internal constructor(view: View, createdConstraints: MutableList<Constraint>,
                                           private val types: List<ConstraintType>) : ConstraintMakerProvider(view, createdConstraints) {

    private val parentViewOrError: View
        get() = view.parent as? View ?: throw MissingParentException(view)

    fun equalTo(variable: ConstraintVariable): Constraint {
        return Constraint(variable, ConstraintOperator.equal)
    }

    fun equalTo(value: Number): Constraint {
        return Constraint(value, ConstraintOperator.equal)
    }

    fun equalTo(view: View): Constraint {
        return Constraint(view, ConstraintOperator.equal)
    }

    fun equalToSuperview(): Constraint {
        return Constraint(parentViewOrError, ConstraintOperator.equal)
    }

    fun lessThanOrEqualTo(variable: ConstraintVariable): Constraint {
        return Constraint(variable, ConstraintOperator.lessOrEqual)
    }

    fun lessThanOrEqualTo(value: Number): Constraint {
        return Constraint(value, ConstraintOperator.lessOrEqual)
    }

    fun lessThanOrEqualTo(view: View): Constraint {
        return Constraint(view, ConstraintOperator.lessOrEqual)
    }

    fun lessThanOrEqualToSuperview(): Constraint {
        return Constraint(parentViewOrError, ConstraintOperator.lessOrEqual)
    }

    fun greaterThanOrEqualTo(variable: ConstraintVariable): Constraint {
        return Constraint(variable, ConstraintOperator.greaterOrEqual)
    }

    fun greaterThanOrEqualTo(value: Number): Constraint {
        return Constraint(value, ConstraintOperator.greaterOrEqual)
    }

    fun greaterThanOrEqualTo(view: View): Constraint {
        return Constraint(view, ConstraintOperator.greaterOrEqual)
    }

    fun greaterThanOrEqualToSuperview(): Constraint {
        return Constraint(parentViewOrError, ConstraintOperator.greaterOrEqual)
    }

    override fun ConstraintMaker(type: ConstraintType): ConstraintMaker {
        return ConstraintMaker(view, createdConstraints, types + type)
    }

    private fun Constraint(variable: ConstraintVariable, operator: ConstraintOperator): Constraint {
        return Constraint(types.distinct().map { ConstraintItem(ConstraintVariable(view, it), operator, variable) })
    }

    private fun Constraint(view: View, operator: ConstraintOperator): Constraint {
        return Constraint(types.distinct().map {
            ConstraintItem(ConstraintVariable(this.view, it), operator, ConstraintVariable(view, it))
        })
    }

    private fun Constraint(value: Number, operator: ConstraintOperator): Constraint {
        return Constraint(types.distinct().map { ConstraintItem(value, operator, it) })
    }

    private fun ConstraintItem(value: Number, operator: ConstraintOperator, type: ConstraintType): ConstraintItem {
        val variableType = when (type) {
            ConstraintType.width, ConstraintType.height -> null
            ConstraintType.centerX, ConstraintType.centerXWithMargins -> ConstraintType.left
            ConstraintType.centerY, ConstraintType.centerYWithMargins -> ConstraintType.top
            ConstraintType.leftMargin -> ConstraintType.left
            ConstraintType.topMargin -> ConstraintType.top
            ConstraintType.rightMargin -> ConstraintType.right
            ConstraintType.bottomMargin -> ConstraintType.bottom
            else -> type
        }
        return ConstraintItem(ConstraintVariable(view, type), operator,
                variableType?.let { ConstraintVariable(parentViewOrError, type) }, value)
    }

    private fun Constraint(constraintItems: List<ConstraintItem>): Constraint {
        val constraint = Constraint(view, constraintItems)
        createdConstraints.add(constraint)
        return constraint
    }
}