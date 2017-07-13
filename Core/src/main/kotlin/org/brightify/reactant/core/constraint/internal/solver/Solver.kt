package org.brightify.reactant.core.constraint.internal.solver

import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.exception.UnsatisfiableConstraintException
import org.brightify.reactant.core.constraint.internal.ConstraintItem
import org.brightify.reactant.core.constraint.internal.ConstraintOperator
import org.brightify.reactant.core.constraint.internal.util.isAlmostZero

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Solver {

    private class Tag(val marker: Symbol, val other: Symbol?)

    private val tagForEquation = HashMap<Equation, Tag>()
    private val rows = HashMap<Symbol, Row>()
    private val variables = HashMap<ConstraintVariable, Symbol>()
    private val objective = Row()
    private var artificialRow: Row? = null

    fun addAll(solver: Solver) {
        tagForEquation.putAll(solver.tagForEquation)
        rows.putAll(solver.rows)
        variables.putAll(solver.variables)

        solver.tagForEquation.forEach { (equation, tag) ->
            when (equation.operator) {
                ConstraintOperator.lessOrEqual, ConstraintOperator.greaterOrEqual -> {
                    if (equation.priority.priority < ConstraintPriority.required.priority) {
                        objective.insert(tag.other!!, equation.priority.priority.toDouble())
                    }
                }
                else -> {
                    if (equation.priority.priority < ConstraintPriority.required.priority) {
                        objective.insert(tag.marker, equation.priority.priority.toDouble())
                        objective.insert(tag.other!!, equation.priority.priority.toDouble())
                    }
                }
            }
        }
        optimize(objective)
    }

    fun addConstraint(constraint: Constraint) {
        val errorItems = ArrayList<ConstraintItem>()
        constraint.constraintItems.forEach {
            try {
                addEquation(it.equation)
            } catch(_: UnsatisfiableConstraintEquationException) {
                errorItems.add(it)
            }
        }
        if (!errorItems.isEmpty()) {
            throw UnsatisfiableConstraintException(Constraint(constraint.view, errorItems))
        }
    }

    fun removeConstraint(constraint: Constraint) {
        constraint.constraintItems.forEach { removeEquation(it.equation) }
    }

    fun addEquation(equation: Equation) {
        if (tagForEquation.containsKey(equation)) {
            return
        }

        val (row, tag) = createRow(equation)
        var subject = chooseSubject(row, tag)

        if (subject == null && row.symbols.keys.all { it.type == Symbol.Type.dummy }) {
            if (row.constant.isAlmostZero) {
                subject = tag.marker
            } else {
                throw UnsatisfiableConstraintEquationException(equation)
            }
        }

        if (subject != null) {
            row.solveFor(subject)
            substitute(subject, row)
            rows[subject] = row
        } else if (!addWithArtificialVariable(row)) {
            throw UnsatisfiableConstraintEquationException(equation)
        }

        tagForEquation[equation] = tag
        optimize(objective)
    }

    fun removeEquation(equation: Equation) {
        val tag = tagForEquation[equation] ?: return

        tagForEquation.remove(equation)
        removeConstraintEffects(equation, tag)

        if (rows.containsKey(tag.marker)) {
            rows.remove(tag.marker)
        } else {
            getMarkerLeavingSymbol(tag.marker)?.let { symbol ->
                rows[symbol]?.let { row ->
                    rows.remove(symbol)
                    row.solveFor(symbol, tag.marker)
                    substitute(tag.marker, row)
                }
            }
        }
        optimize(objective)
    }

    fun getValueForVariable(variable: ConstraintVariable): Double {
        return variables[variable]?.let { rows[it]?.constant } ?: 0.0
    }

    private fun removeConstraintEffects(equation: Equation, tag: Tag) {
        if (tag.marker.type == Symbol.Type.error) {
            removeMarkerEffects(tag.marker, equation.priority)
        } else if (tag.other?.type == Symbol.Type.error) {
            removeMarkerEffects(tag.other, equation.priority)
        }
    }

    private fun removeMarkerEffects(marker: Symbol, priority: ConstraintPriority) {
        val equation = rows[marker]
        if (equation != null) {
            objective.insert(equation, (-priority.priority).toDouble())
        } else {
            objective.insert(marker, (-priority.priority).toDouble())
        }
    }

    private fun getMarkerLeavingSymbol(marker: Symbol): Symbol? {
        var r1 = Double.MAX_VALUE
        var r2 = Double.MAX_VALUE

        var first: Symbol? = null
        var second: Symbol? = null
        var third: Symbol? = null

        rows.forEach { (symbol, row) ->
            val coefficient = row.coefficientFor(marker)
            if (coefficient != 0.0) {
                if (symbol.type == Symbol.Type.external) {
                    third = symbol
                } else if (coefficient < 0.0) {
                    val r = -row.constant / coefficient
                    if (r < r1) {
                        r1 = r
                        first = symbol
                    }
                } else {
                    val r = row.constant / coefficient
                    if (r < r2) {
                        r2 = r
                        second = symbol
                    }
                }
            }
        }
        return first ?: second ?: third
    }

    private fun createRow(equation: Equation): Pair<Row, Tag> {
        val row = Row(equation.constant)
        equation.terms.forEach {
            val symbol = getVarSymbol(it.variable)

            val rowWithSymbol = rows[symbol]
            if (rowWithSymbol != null) {
                row.insert(rowWithSymbol, it.coefficient)
            } else {
                row.insert(symbol, it.coefficient)
            }
        }

        val marker: Symbol
        var other: Symbol? = null
        when (equation.operator) {
            ConstraintOperator.lessOrEqual, ConstraintOperator.greaterOrEqual -> {
                val coefficient = if (equation.operator == ConstraintOperator.lessOrEqual) 1.0 else -1.0
                marker = Symbol(Symbol.Type.slack)
                row.insert(marker, coefficient)
                if (equation.priority.priority < ConstraintPriority.required.priority) {
                    other = Symbol(Symbol.Type.error)
                    row.insert(other, -coefficient)
                    objective.insert(other, equation.priority.priority.toDouble())
                }
            }
            else -> {
                if (equation.priority.priority < ConstraintPriority.required.priority) {
                    marker = Symbol(Symbol.Type.error)
                    other = Symbol(Symbol.Type.error)
                    row.insert(marker, -1.0)
                    row.insert(other, 1.0)
                    objective.insert(marker, equation.priority.priority.toDouble())
                    objective.insert(other, equation.priority.priority.toDouble())
                } else {
                    marker = Symbol(Symbol.Type.dummy)
                    row.insert(marker)
                }
            }
        }

        if (row.constant < 0.0) {
            row.reverseSign()
        }
        return Pair(row, Tag(marker, other))
    }

    private fun chooseSubject(row: Row, tag: Tag): Symbol? {
        row.symbols.keys.firstOrNull { it.type == Symbol.Type.external }?.let { return it }

        if ((tag.marker.type == Symbol.Type.slack || tag.marker.type == Symbol.Type.error) && row.coefficientFor(tag.marker) < 0.0) {
            return tag.marker
        }
        if ((tag.other?.type == Symbol.Type.slack || tag.other?.type == Symbol.Type.error) && row.coefficientFor(tag.other) < 0.0) {
            return tag.other
        }

        return null
    }

    private fun addWithArtificialVariable(row: Row): Boolean {
        val artificialVariable = Symbol(Symbol.Type.slack)
        rows[artificialVariable] = row
        artificialRow = Row(row)
        artificialRow?.let { optimize(it) }
        val success = artificialRow?.constant?.isAlmostZero == true
        artificialRow = null

        val artificialRow = rows[artificialVariable]
        if (artificialRow != null) {
            rows.remove(artificialVariable)
            if (artificialRow.symbols.isEmpty()) {
                return success
            }
            val entering = anyPivotableSymbol(artificialRow) ?: return false
            artificialRow.solveFor(artificialVariable, entering)
            substitute(entering, artificialRow)
            rows[entering] = artificialRow
        }

        rows.values.forEach { it.remove(artificialVariable) }
        objective.remove(artificialVariable)
        return success
    }

    private fun optimize(objective: Row) {
        while (true) {
            val entering = getEnteringSymbol(objective) ?: return
            val leaving = getLeavingSymbol(entering) ?: return

            rows[leaving]?.let { row ->
                rows.remove(leaving)
                row.solveFor(leaving, entering)
                substitute(entering, row)
                rows[entering] = row
            }
        }
    }

    private fun substitute(symbol: Symbol, row: Row) {
        rows.forEach { it.value.substitute(symbol, row) }
        objective.substitute(symbol, row)
        artificialRow?.substitute(symbol, row)
    }

    private fun getEnteringSymbol(objective: Row): Symbol? {
        objective.symbols.forEach { (symbol, value) ->
            if (symbol.type != Symbol.Type.dummy && value < 0) {
                return symbol
            }
        }
        return null
    }

    private fun anyPivotableSymbol(row: Row): Symbol? {
        return row.symbols.keys.firstOrNull { it.type == Symbol.Type.slack || it.type == Symbol.Type.error }
    }

    private fun getLeavingSymbol(entering: Symbol): Symbol? {
        var ratio = Double.MAX_VALUE
        var symbol: Symbol? = null

        rows.forEach {
            if (it.key.type != Symbol.Type.external) {
                val temp = it.value.coefficientFor(entering)
                if (temp < 0) {
                    val temp_ratio = -it.value.constant / temp
                    if (temp_ratio < ratio) {
                        ratio = temp_ratio
                        symbol = it.key
                    }
                }
            }
        }
        return symbol
    }

    private fun getVarSymbol(variable: ConstraintVariable): Symbol {
        var symbol = variables[variable]
        if (symbol == null) {
            symbol = Symbol(Symbol.Type.external)
            variables[variable] = symbol
        }
        return symbol
    }
}
