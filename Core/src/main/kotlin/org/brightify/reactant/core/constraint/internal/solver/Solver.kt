package org.brightify.reactant.core.constraint.internal.solver

import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.exception.UnsatisfiableConstraintException
import org.brightify.reactant.core.constraint.internal.ConstraintItem
import org.brightify.reactant.core.constraint.internal.ConstraintOperator
import java.util.HashSet

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Solver {

    private val errorSymbols = LinkedHashMap<Equation, HashSet<Symbol>>()
    private val objective = Symbol(Symbol.Type.objective)

    private var columns = HashMap<Symbol, HashSet<Symbol>>()
    private var rows = LinkedHashMap<Symbol, Row>().apply { put(objective, Row()) }

    private val symbolForEquation = LinkedHashMap<Equation, Symbol>()
    private val symbolForVariable = HashMap<ConstraintVariable, Symbol>()

    private val equationsToAdd = HashSet<Equation>()
    private val equationsToRemove = HashSet<Equation>()

    fun addConstraint(constraint: Constraint) {
        val errorItems = ArrayList<ConstraintItem>()
        constraint.constraintItems.forEach {
            try {
                addEquation(it.equation)
            } catch(_: UnsatisfiableEquationException) {
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
        equationsToAdd.add(equation)
    }

    fun removeEquation(equation: Equation) {
        if (!equationsToAdd.remove(equation)) {
            equationsToRemove.add(equation)
        }
    }

    fun solve() {
        equationsToRemove.forEach { removeEquationImmediately(it) }
        equationsToAdd.forEach { addEquationImmediately(it) }
        equationsToAdd.clear()
        equationsToRemove.clear()
        optimize(objective)
    }

    fun getValueForVariable(variable: ConstraintVariable): Double {
        return symbolForVariable[variable]?.let { rows[it]?.constant } ?: 0.0
    }

    private fun addEquationImmediately(equation: Equation) {
        val expression = createRow(equation)
        if (!tryAddingDirectly(expression)) {
            addWithArtificialVariable(expression)
        }
    }

    private fun removeEquationImmediately(equation: Equation) {
        val marker = symbolForEquation.remove(equation) ?: return

        removeEquationEffects(equation, marker)

        removeRow(marker)
        errorSymbols[equation]?.forEach {
            if (it != marker) {
                removeColumn(it)
            }
        }

        errorSymbols.remove(equation)
    }

    private fun createRow(equation: Equation): Row {
        val row = Row(-equation.constant)

        equation.terms.forEach {
            val symbol = getSymbolForVariable(it.variable)

            val rowWithSymbol = rows[symbol]
            if (rowWithSymbol != null) {
                row.addExpression(rowWithSymbol, it.coefficient)
            } else {
                row.addVariable(symbol, it.coefficient)
            }
        }

        if (equation.operator == ConstraintOperator.equal) {
            if (equation.priority == ConstraintPriority.required) {
                val symbol = Symbol(Symbol.Type.dummy)
                row.addVariable(symbol, 1.0)
                symbolForEquation.put(equation, symbol)
            } else {
                val slackPlus = Symbol(Symbol.Type.slack)
                val slackMinus = Symbol(Symbol.Type.slack)

                row.addVariable(slackPlus, -1.0)
                row.addVariable(slackMinus, 1.0)
                symbolForEquation.put(equation, slackPlus)
                rows[objective]?.let {
                    it.addVariable(slackPlus, equation.priority.value.toDouble())
                    onSymbolAdded(slackPlus, objective)
                    it.addVariable(slackMinus, equation.priority.value.toDouble())
                    onSymbolAdded(slackMinus, objective)
                    insertErrorSymbol(equation, slackMinus)
                    insertErrorSymbol(equation, slackPlus)
                }
            }
        } else {
            if (equation.operator == ConstraintOperator.lessOrEqual) {
                row.multiplyBy(-1.0)
            }
            val slack = Symbol(Symbol.Type.slack)
            row.addVariable(slack, -1.0)
            symbolForEquation.put(equation, slack)
            if (equation.priority != ConstraintPriority.required) {
                val slackMinus = Symbol(Symbol.Type.slack)
                row.addVariable(slackMinus, 1.0)
                rows[objective]?.addVariable(slackMinus, equation.priority.value.toDouble())
                insertErrorSymbol(equation, slackMinus)
                onSymbolAdded(slackMinus, objective)
            }
        }

        if (row.constant < 0) {
            row.multiplyBy(-1.0)
        }

        return row
    }

    private fun removeEquationEffects(equation: Equation, marker: Symbol) {
        errorSymbols[equation]?.forEach {
            val row = rows[it]
            if (row != null) {
                rows[objective]?.addExpression(row, -equation.priority.value.toDouble(), objective, this)
            } else {
                rows[objective]?.addVariable(it, -equation.priority.value.toDouble(), objective, this)
            }
        }

        if (rows[marker] == null) {
            columns[marker]?.let { column ->
                var leaving: Symbol? = null
                var minRatio = 0.0
                column.forEach {
                    if (it.isRestricted) {
                        rows[it]?.let { row ->
                            val coefficient = row.coefficientFor(marker)
                            if (coefficient < 0.0) {
                                val r = -row.constant / coefficient
                                if (leaving == null || r < minRatio) {
                                    minRatio = coefficient
                                    leaving = it
                                }
                            }
                        }
                    }
                }
                if (leaving == null) {
                    column.forEach {
                        if (it.isRestricted) {
                            rows[it]?.let { row ->
                                val coefficient = row.coefficientFor(marker)
                                val r = row.constant / coefficient
                                if (leaving == null || r < minRatio) {
                                    minRatio = coefficient
                                    leaving = it
                                }
                            }
                        }
                    }
                }

                if (leaving == null) {
                    if (column.size == 0) {
                        removeColumn(marker)
                    } else {
                        leaving = column.first()
                    }
                }

                leaving?.let { pivot(marker, it) }
            }
        }
    }

    private fun tryAddingDirectly(row: Row): Boolean {
        val subject = chooseSubject(row) ?: return false
        row.newSubject(subject)
        if (columns.containsKey(subject)) {
            substituteOut(subject, row)
        }
        addRow(subject, row)
        return true
    }

    private fun chooseSubject(row: Row): Symbol? {
        row.symbols.keys.firstOrNull { !it.isRestricted }?.let { return it }

        row.symbols.forEach { (symbol, value) ->
            if (symbol.type != Symbol.Type.dummy && value < 0.0 && columns[symbol] == null) {
                return symbol
            }
        }

        var subject: Symbol? = null
        var coefficient = 0.0
        row.symbols.forEach { (symbol, value) ->
            if (symbol.type != Symbol.Type.dummy) {
                return null
            } else if (!columns.containsKey(symbol)) {
                subject = symbol
                coefficient = value
            }
        }

        // TODO Error

        if (coefficient > 0.0) {
            row.multiplyBy(-1.0)
        }

        return subject
    }

    private fun addWithArtificialVariable(row: Row) {
        val artificialVariable = Symbol(Symbol.Type.slack)
        val objectiveVariable = Symbol(Symbol.Type.objective)

        addRow(objectiveVariable, Row(row))
        addRow(artificialVariable, row)

        optimize(objectiveVariable)

        rows[artificialVariable]?.let {
            val entering = it.symbols.keys.firstOrNull { it.type == Symbol.Type.slack } ?: throw RuntimeException() // TODO UnsatisfiableEquationException
            pivot(entering, artificialVariable)
        }
        removeColumn(artificialVariable)
        removeRow(objectiveVariable)
    }

    private fun optimize(symbol: Symbol) {
        val row = rows[symbol] ?: return

        while (true) {
            val entering = getEnteringSymbol(row) ?: return
            val leaving = getLeavingSymbol(entering) ?: return
            pivot(entering, leaving)
        }
    }

    private fun getEnteringSymbol(row: Row): Symbol? {
        row.symbols.forEach { (symbol, value) ->
            if (symbol.type == Symbol.Type.slack && value < 0) {
                return symbol
            }
        }
        return null
    }

    private fun getLeavingSymbol(entering: Symbol): Symbol? {
        var minRatio = java.lang.Double.MAX_VALUE
        var symbol: Symbol? = null

        columns[entering]?.forEach {
            if (it.type == Symbol.Type.slack) {
                rows[it]?.let { row ->
                    val coefficient = row.coefficientFor(entering)
                    if (coefficient < 0.0) {
                        val ratio = -row.constant / coefficient
                        if (ratio < minRatio) {
                            minRatio = ratio
                            symbol = it
                        }
                    }
                }
            }
        }
        return symbol
    }

    private fun pivot(entrySymbol: Symbol, exitSymbol: Symbol) {
        removeRow(exitSymbol)?.let {
            it.changeSubject(exitSymbol, entrySymbol)
            substituteOut(entrySymbol, it)
            addRow(entrySymbol, it)
        }
    }

    private fun insertErrorSymbol(equation: Equation, symbol: Symbol) {
        var symbols = errorSymbols[equation]
        if (symbols == null) {
            symbols = HashSet<Symbol>()
            errorSymbols.put(equation, symbols)
        }
        symbols.add(symbol)
    }

    internal fun onSymbolRemoved(symbol: Symbol, subject: Symbol) {
        columns[symbol]?.remove(subject)
    }

    internal fun onSymbolAdded(symbol: Symbol, subject: Symbol) {
        insertColumnVariable(symbol, subject)
    }

    private fun insertColumnVariable(columnVariable: Symbol, rowVariable: Symbol) {
        var rows = columns[columnVariable]
        if (rows == null) {
            rows = HashSet<Symbol>()
            columns.put(columnVariable, rows)
        }
        rows.add(rowVariable)
    }

    private fun addRow(symbol: Symbol, row: Row) {
        rows.put(symbol, row)

        row.symbols.keys.forEach { insertColumnVariable(it, symbol) }
    }

    private fun removeColumn(symbol: Symbol) {
        columns.remove(symbol)?.forEach { rows[it]?.symbols?.remove(symbol) }
    }

    private fun removeRow(symbol: Symbol): Row? {
        val row = rows.remove(symbol)
        row?.symbols?.keys?.forEach { columns[it]?.remove(symbol) }
        return row
    }

    private fun substituteOut(oldSymbol: Symbol, row: Row) {
        columns.remove(oldSymbol)?.forEach {
            rows[it]?.substituteOut(oldSymbol, row, it, this)
        }
    }

    private fun getSymbolForVariable(variable: ConstraintVariable): Symbol {
        var symbol = symbolForVariable[variable]
        if (symbol == null) {
            symbol = Symbol(Symbol.Type.external)
            symbolForVariable[variable] = symbol
        }
        return symbol
    }

    private val Symbol.isRestricted: Boolean
        get() = type == Symbol.Type.slack || type == Symbol.Type.dummy
}
