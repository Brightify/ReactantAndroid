package org.brightify.reactant.core.constraint

import org.brightify.reactant.core.constraint.solver.Equation
import org.brightify.reactant.core.constraint.solver.Term
import kotlin.properties.Delegates.observable

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ConstraintItem(val leftVariable: ConstraintVariable, val operator: ConstraintOperator,
                              val rightVariable: ConstraintVariable? = null, offset: Number = 0) {

    var multiplier: Number by observable(1 as Number) { _, _, _ ->
        equation = createEquation()
    }

    var offset: Number by observable(offset) { _, _, _ ->
        equation = createEquation()
    }

    var priority: ConstraintPriority by observable(ConstraintPriority.required) { _, _, _ ->
        equation = createEquation()
    }

    val type: ConstraintType
        get() = leftVariable.type

    var equation = createEquation()

    override fun toString(): String {
        val rightVariable = rightVariable?.let { Term(multiplier, rightVariable).toString() + " " } ?: ""
        val offset = offset.toDouble()
        val absOffset = Math.abs(offset)
        val offsetString: String
        if (offset == 0.0 && !rightVariable.isEmpty()) {
            offsetString = ""
        } else if (offset < 0) {
            offsetString = "- $absOffset "
        } else if (rightVariable.isEmpty()) {
            offsetString = "$absOffset "
        } else {
            offsetString = "+ $absOffset "
        }

        return "{$leftVariable} $operator $rightVariable$offsetString($priority)"
    }

    private fun createEquation(): Equation {
        return Equation(
                leftTerms = listOf(Term(leftVariable)),
                operator = operator,
                rightConstant = offset,
                rightTerms = rightVariable?.let { listOf(Term(multiplier, it)) } ?: emptyList(),
                priority = priority
        )
    }
}
