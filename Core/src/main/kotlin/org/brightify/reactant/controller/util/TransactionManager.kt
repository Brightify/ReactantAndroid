package org.brightify.reactant.controller.util

import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class TransactionManager {

    private val transactions = ArrayList<() -> Unit>()

    val isInTransaction: Boolean
        get() = !transactions.isEmpty()

    var enabled: Boolean by onChange(false) { _, _, _ ->
        if (enabled) {
            commitTransactions()
        }
    }

    inline fun <T> transaction(crossinline action: () -> T): T? {
        var result: T? = null
        transactions.add { result = action() }
        if (transactions.size == 1 && enabled) {
            commitTransactions()
        }
        return result
    }

    private fun commitTransactions() {
        var i = 0
        while (i < transactions.size) {
            transactions[i]()
            i++
        }
        transactions.clear()
    }
}