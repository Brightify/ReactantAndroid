package org.brightify.reactant.autolayout.internal

import org.brightify.reactant.autolayout.ConstraintOperator
import org.brightify.reactant.autolayout.ConstraintPriority
import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.autolayout.internal.solver.Equation
import org.brightify.reactant.autolayout.internal.solver.Term
import org.brightify.reactant.core.util.onChange
import kotlin.math.abs

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class ConstraintItem(val leftVariable: ConstraintVariable, operator: ConstraintOperator,
                              val rightVariable: ConstraintVariable? = null, offset: Number = 0) {

    var multiplier: Number by onChange(1 as Number) { _, _, _ ->
        equation = createEquation()
    }

    var operator: ConstraintOperator by onChange(operator) { _, _, _ ->
        equation = createEquation()
    }

    var offset: Number by onChange(offset) { _, _, _ ->
        equation = createEquation()
    }

    var priority: ConstraintPriority by onChange(ConstraintPriority.required) { _, _, _ ->
        equation = createEquation()
    }

    val type: ConstraintType
        get() = leftVariable.type

    var equation = createEquation()
        private set

    override fun toString(): String {
        val rightVariable = rightVariable?.let { Term(multiplier.toDouble(), rightVariable).toString() + " " } ?: ""
        val offset = offset.toDouble()
        val absOffset = abs(offset)
        val offsetString: String
        offsetString = if (offset == 0.0 && rightVariable.isNotEmpty()) {
            ""
        } else if (offset < 0) {
            "- $absOffset "
        } else if (rightVariable.isEmpty()) {
            "$absOffset "
        } else {
            "+ $absOffset "
        }

        return "{$leftVariable} $operator $rightVariable$offsetString($priority)"
    }

    private fun createEquation(): Equation {
        val terms = ArrayList<Term>()
        terms.addAll(Term(leftVariable).baseTerms)
        rightVariable?.let {
            terms.addAll(Term(-multiplier.toDouble(), it).baseTerms)
        }
        return Equation(terms, operator, offset.toDouble(), priority)
    }
}
