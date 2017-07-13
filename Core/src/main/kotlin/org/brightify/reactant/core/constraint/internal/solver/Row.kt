package org.brightify.reactant.core.constraint.internal.solver

import org.brightify.reactant.core.constraint.internal.util.isAlmostZero

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Row(var constant: Double = 0.0) {

    var symbols = LinkedHashMap<Symbol, Double>()

    constructor(other: Row) : this(other.constant) {
        symbols = LinkedHashMap(other.symbols)
    }

    fun add(value: Double): Double {
        constant += value
        return constant
    }

    fun insert(symbol: Symbol, coefficient: Double = 1.0) {
        symbols[symbol] = (symbols[symbol] ?: 0.0) + coefficient

        if (symbols[symbol]?.isAlmostZero == true) {
            symbols.remove(symbol)
        }
    }

    fun insert(other: Row, coefficient: Double = 1.0) {
        this.constant += other.constant * coefficient

        other.symbols.forEach { (key, value) -> insert(key, value * coefficient) }
    }

    fun remove(symbol: Symbol) {
        symbols.remove(symbol)
    }

    fun reverseSign() {
        constant *= -1

        symbols.forEach { (key, value) -> symbols[key] = -value }
    }

    fun solveFor(symbol: Symbol) {
        val coefficient = -1.0 / (symbols[symbol] ?: 1.0)
        symbols.remove(symbol)
        constant *= coefficient

        symbols.forEach { (key, value) -> symbols[key] = value * coefficient }
    }

    fun solveFor(lhs: Symbol, rhs: Symbol) {
        insert(lhs, -1.0)
        solveFor(rhs)
    }

    fun coefficientFor(symbol: Symbol): Double {
        return symbols[symbol] ?: 0.0
    }

    fun substitute(symbol: Symbol, row: Row) {
        symbols.remove(symbol)?.let { insert(row, it) }
    }
}
