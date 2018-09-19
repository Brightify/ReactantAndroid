package org.brightify.reactant.autolayout.internal.solver

import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.autolayout.internal.ConstraintItem

/**
 * @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class BackgroundSolver(private val solver: SimplexSolver) {

    private data class Operation(val add: Boolean, val equation: Equation, val constraintItem: ConstraintItem)

    private var pendingOperations = ArrayList<Operation>()
    private var backupArray = ArrayList<Operation>()

    private val solvedMonitor = Object()
    private val pendingOperationsMonitor = Object()

    private var solved = true

    private val thread = Thread {
        while (true) {
            synchronized(pendingOperationsMonitor) {
                while (pendingOperations.isEmpty()) {
                    pendingOperationsMonitor.wait()
                }
                val operations = pendingOperations
                pendingOperations = backupArray
                backupArray = operations
            }

            backupArray.forEach {
                if (it.add) {
                    solver.addEquation(it.equation, it.constraintItem)
                } else {
                    solver.removeEquation(it.equation)
                }
            }
            backupArray.clear()

            solver.solve()

            synchronized(solvedMonitor) {
                synchronized(pendingOperationsMonitor) {
                    if (pendingOperations.isEmpty()) {
                        solved = true
                        solvedMonitor.notify()
                    }
                }
            }
        }
    }

    init {
        thread.start()
    }

    fun addConstraint(constraint: Constraint) {
        synchronized(solvedMonitor) {
            synchronized(pendingOperationsMonitor) {
                solved = false
                constraint.constraintItems.forEach {
                    pendingOperations.add(Operation(true, it.equation, it))
                }
                pendingOperationsMonitor.notify()
            }
        }
    }

    fun removeConstraint(constraint: Constraint) {
        synchronized(solvedMonitor) {
            synchronized(pendingOperationsMonitor) {
                solved = false
                constraint.constraintItems.forEach {
                    pendingOperations.add(Operation(false, it.equation, it))
                }
                pendingOperationsMonitor.notify()
            }
        }
    }

    fun getValueForVariable(variable: ConstraintVariable): Double {
        if (!solved) {
            synchronized(solvedMonitor) {
                while (!solved) {
                    solvedMonitor.wait()
                }
            }
        }

        return solver.getValueForVariable(variable)
    }
}