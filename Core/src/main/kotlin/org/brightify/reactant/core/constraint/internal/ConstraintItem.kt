package org.brightify.reactant.core.constraint.internal

import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.solver.Equation
import org.brightify.reactant.core.constraint.internal.solver.Term
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
        val rightVariable = rightVariable?.let { Term(multiplier.toDouble(), rightVariable).toString() + " " } ?: ""
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
            offsetString = "+  "
        }

        return "{$leftVariable} $operator $rightVariable$offsetString($priority)"
    }

    private fun createEquation(): Equation {
        val terms = mutableListOf(Term(leftVariable))
        rightVariable?.let { terms.add(Term(-multiplier.toDouble(), it)) }
        return Equation(-offset.toDouble(), terms, operator, priority)
    }
}
