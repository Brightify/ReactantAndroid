package org.brightify.reactant.core.constraint.solver

import org.brightify.reactant.core.constraint.ConstraintOperator
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import java.util.LinkedHashMap

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Solver {

    private class Tag(val marker: Symbol, val other: Symbol?)

    private val tagForEquation = LinkedHashMap<Equation, Tag>()
    private val rows = LinkedHashMap<Symbol, Row>()
    private val variables = LinkedHashMap<ConstraintVariable, Symbol>()
    private val objective = Row()

    fun addEquation(equation: Equation) {
        if (tagForEquation.containsKey(equation)) {
            return
        }

        val (row, tag) = createRow(equation)
        var subject = chooseSubject(row, tag)

        if (subject == null && row.symbols.all { it.key.type == Symbol.Type.dummy }) {
            if (row.constant.isAlmostZero) {
                subject = tag.marker
            } else {
                throw UnsatisfiableConstraintException(equation)
            }
        }

        if (subject != null) {
            row.solveFor(subject)
            substitute(subject, row)
            rows[subject] = row
        } else if (!addWithArtificialVariable(row)) {
            throw UnsatisfiableConstraintException(equation)
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

    fun hasEquation(equation: Equation): Boolean {
        return tagForEquation.containsKey(equation)
    }

    fun valueForVariable(variable: ConstraintVariable): Double {
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
        equation.terms
                .groupBy { it.variable }
                .map { Term(it.value.map { it.coefficient.toDouble() }.reduce { acc, coefficient -> acc + coefficient }, it.key) }
                .filter { !it.coefficient.toDouble().isAlmostZero }
                .forEach {
                    val symbol = getVarSymbol(it.variable)

                    val rowWithSymbol = rows[symbol]
                    if (rowWithSymbol != null) {
                        row.insert(rowWithSymbol, it.coefficient.toDouble())
                    } else {
                        row.insert(symbol, it.coefficient.toDouble())
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
        val rowCopy = Row(row)
        optimize(rowCopy)
        val success = rowCopy.constant.isAlmostZero

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

            getLeavingSymbol(entering)?.let { leaving ->
                rows[leaving]?.let { row ->
                    rows.remove(leaving)
                    row.solveFor(leaving, entering)
                    substitute(entering, row)
                    rows[entering] = row
                }
            }
        }
    }

    private fun substitute(symbol: Symbol, row: Row) {
        rows.values.forEach { value -> value.substitute(symbol, row) }
        objective.substitute(symbol, row)
    }

    private fun getEnteringSymbol(objective: Row): Symbol? {
        return objective.symbols.filter { it.key.type != Symbol.Type.dummy && it.value < 0.0 }.map { it.key }.firstOrNull()
    }

    private fun anyPivotableSymbol(row: Row): Symbol? {
        return row.symbols.map { it.key }.firstOrNull { it.type == Symbol.Type.slack || it.type == Symbol.Type.error }
    }

    private fun getLeavingSymbol(entering: Symbol): Symbol? {
        var ratio = Double.MAX_VALUE
        var symbol: Symbol? = null

        rows.keys.filter { it.type != Symbol.Type.external }.forEach {
            val testedRow = rows[it]
            if (testedRow != null) {
                val temp = testedRow.coefficientFor(entering)
                if (temp < 0) {
                    val temp_ratio = -testedRow.constant / temp
                    if (temp_ratio < ratio) {
                        ratio = temp_ratio
                        symbol = it
                    }
                }
            }
        }
        return symbol
    }

    private fun getVarSymbol(variable: ConstraintVariable): Symbol {
        return variables[variable] ?: {
            val symbol = Symbol(Symbol.Type.external)
            variables[variable] = symbol
            symbol
        }()
    }
}
