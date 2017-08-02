package org.brightify.reactant.controller.util

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class TransactionManager {

    private val transactions = ArrayList<() -> Unit>()

    val isInTransaction: Boolean
        get() = !transactions.isEmpty()

    inline fun <T> transaction(crossinline action: () -> T): T? {
        var result: T? = null
        transactions.add { result = action() }
        if (transactions.size == 1) {
            var i = 0
            while (i < transactions.size) {
                transactions[i]()
                i++
            }
            transactions.clear()
        }
        return result
    }
}