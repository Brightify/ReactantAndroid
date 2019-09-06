package org.brightify.reactant.autolayout.internal.solver

import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.autolayout.internal.ConstraintItem
import java.util.concurrent.Executors

/**
 * @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class BackgroundSolver(private val solver: SimplexSolver) {

    private data class Operation(
        val add: Boolean,
        val equation: Equation,
        val constraintItem: ConstraintItem
    )

    private var pendingOperations = ArrayList<Operation>()
    private var backupArray = ArrayList<Operation>()

    private val solvedMonitor = Object()

    private var solved = true
    private var queuedWorkersCount = 0

    private val threadPoolExecutor = Executors.newSingleThreadExecutor()

    fun addConstraint(constraint: Constraint) {
        synchronized(solvedMonitor) {
            constraint.constraintItems.forEach {
                pendingOperations.add(Operation(true, it.equation, it))
            }
            updateSolver()
        }
    }

    fun removeConstraint(constraint: Constraint) {
        synchronized(solvedMonitor) {
            constraint.constraintItems.forEach {
                pendingOperations.add(Operation(false, it.equation, it))
            }
            updateSolver()
        }
    }

    fun getValueForVariable(variable: ConstraintVariable): Double {
        synchronized(solvedMonitor) {
            while (!solved) {
                solvedMonitor.wait()
            }
            return solver.getValueForVariable(variable)
        }
    }

    private fun updateSolver() {
        solved = false

        if (queuedWorkersCount < 2) {
            threadPoolExecutor.submit {
                synchronized(solvedMonitor) {
                    val operations = pendingOperations
                    pendingOperations = backupArray
                    backupArray = operations
                }

                if (backupArray.isNotEmpty()) {
                    backupArray.forEach {
                        if (it.add) {
                            solver.addEquation(it.equation, it.constraintItem)
                        } else {
                            solver.removeEquation(it.equation)
                        }
                    }
                    backupArray.clear()

                    solver.solve()
                }

                synchronized(solvedMonitor) {
                    if (pendingOperations.isEmpty()) {
                        solved = true
                        solvedMonitor.notify()
                    }
                    queuedWorkersCount--
                }
            }
            queuedWorkersCount++
        }
    }
}