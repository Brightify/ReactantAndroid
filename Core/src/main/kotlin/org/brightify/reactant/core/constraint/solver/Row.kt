package org.brightify.reactant.core.constraint.solver

import java.util.LinkedHashMap

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Row(var constant: Double = 0.0) {

    private var symbolMap = LinkedHashMap<Symbol, Double>()

    val symbols: Map<Symbol, Double>
        get() = symbolMap

    constructor(other: Row) : this(other.constant) {
        symbolMap = LinkedHashMap(other.symbolMap)
    }

    fun add(value: Double): Double {
        constant += value
        return constant
    }

    fun insert(symbol: Symbol, coefficient: Double = 1.0) {
        symbolMap[symbol] = (symbolMap[symbol] ?: 0.0) + coefficient

        if (symbolMap[symbol]?.isAlmostZero == true) {
            symbolMap.remove(symbol)
        }
    }

    fun insert(other: Row, coefficient: Double = 1.0) {
        this.constant += other.constant * coefficient

        other.symbolMap.forEach { (key, value) -> insert(key, value * coefficient) }
    }

    fun remove(symbol: Symbol) {
        symbolMap.remove(symbol)
    }

    fun reverseSign() {
        constant *= -1

        symbolMap.forEach { (key, value) -> symbolMap[key] = -value }
    }

    fun solveFor(symbol: Symbol) {
        val coefficient = -1 / (symbolMap[symbol] ?: 1.0)
        symbolMap.remove(symbol)
        constant *= coefficient

        symbolMap.forEach { (key, value) -> symbolMap[key] = value * coefficient }
    }

    fun solveFor(lhs: Symbol, rhs: Symbol) {
        insert(lhs, -1.0)
        solveFor(rhs)
    }

    fun coefficientFor(symbol: Symbol): Double {
        return symbolMap[symbol] ?: 0.0
    }

    fun substitute(symbol: Symbol, row: Row) {
        val coefficient = symbolMap[symbol] ?: 0.0
        symbolMap.remove(symbol)
        insert(row, coefficient)
    }
}
