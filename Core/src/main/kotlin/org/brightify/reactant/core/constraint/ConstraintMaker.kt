package org.brightify.reactant.core.constraint

import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintMaker internal constructor(view: View, createdConstraints: MutableList<Constraint>,
                                           private val types: List<ConstraintType>) : ConstraintMakerProvider(view, createdConstraints) {

    private val parentViewIdOrError: Int
        get() = (view.parent as? View)?.id ?: throw RuntimeException()

    fun equalTo(variable: ConstraintVariable): Constraint {
        return Constraint(variable, ConstraintOperator.equal)
    }

    fun equalTo(value: Number): Constraint {
        return Constraint(value, ConstraintOperator.equal)
    }

    fun equalTo(view: View): Constraint {
        return Constraint(view.id, ConstraintOperator.equal)
    }

    fun equalToSuperview(): Constraint {
        return Constraint(parentViewIdOrError, ConstraintOperator.equal)
    }

    fun lessThanOrEqualTo(variable: ConstraintVariable): Constraint {
        return Constraint(variable, ConstraintOperator.lessOrEqual)
    }

    fun lessThanOrEqualTo(value: Number): Constraint {
        return Constraint(value, ConstraintOperator.lessOrEqual)
    }

    fun lessThanOrEqualTo(view: View): Constraint {
        return Constraint(view.id, ConstraintOperator.lessOrEqual)
    }

    fun lessThanOrEqualToSuperview(): Constraint {
        return Constraint(parentViewIdOrError, ConstraintOperator.lessOrEqual)
    }

    fun greaterThanOrEqualTo(variable: ConstraintVariable): Constraint {
        return Constraint(variable, ConstraintOperator.greaterOrEqual)
    }

    fun greaterThanOrEqualTo(value: Number): Constraint {
        return Constraint(value, ConstraintOperator.greaterOrEqual)
    }

    fun greaterThanOrEqualTo(view: View): Constraint {
        return Constraint(view.id, ConstraintOperator.greaterOrEqual)
    }

    fun greaterThanOrEqualToSuperview(): Constraint {
        return Constraint(parentViewIdOrError, ConstraintOperator.greaterOrEqual)
    }

    override fun ConstraintMaker(type: ConstraintType): ConstraintMaker {
        return ConstraintMaker(view, createdConstraints, types + type)
    }

    private fun Constraint(variable: ConstraintVariable, operator: ConstraintOperator): Constraint {
        return Constraint(types.distinct().map { ConstraintItem(ConstraintVariable(view.id, it), operator, variable) })
    }

    private fun Constraint(viewId: Int, operator: ConstraintOperator): Constraint {
        return Constraint(types.distinct().map {
            ConstraintItem(ConstraintVariable(view.id, it), operator, ConstraintVariable(viewId, it))
        })
    }

    private fun Constraint(value: Number, operator: ConstraintOperator): Constraint {
        return Constraint(types.distinct().map { ConstraintItem(value, operator, it) })
    }

    private fun ConstraintItem(value: Number, operator: ConstraintOperator, type: ConstraintType): ConstraintItem {
        val variableType = when (type) {
            ConstraintType.width, ConstraintType.height -> null
            ConstraintType.centerX -> ConstraintType.left
            ConstraintType.centerY -> ConstraintType.top
            else -> type
        }
        return ConstraintItem(ConstraintVariable(view.id, type), operator,
                variableType?.let { ConstraintVariable(parentViewIdOrError, type) }, value)
    }

    private fun Constraint(constraintItems: List<ConstraintItem>): Constraint {
        val constraint = Constraint(view, constraintItems)
        createdConstraints.add(constraint)
        return constraint
    }
}