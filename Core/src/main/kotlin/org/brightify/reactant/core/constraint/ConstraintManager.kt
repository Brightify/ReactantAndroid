package org.brightify.reactant.core.constraint

import android.view.View
import org.brightify.reactant.core.constraint.solver.Equation
import org.brightify.reactant.core.constraint.solver.Solver
import org.brightify.reactant.core.constraint.util.ComplexEquationsProvider

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ConstraintManager {

    private var ownSolver = Solver()

    private var delegatedTo: ConstraintManager? = null

    private val managedEquations = HashMap<Int, List<Equation>>()
    private val managedConstraints = HashSet<Constraint>()
    private val managedViews = HashSet<Int>()

    fun addConstraint(constraint: Constraint) {
        if (managedConstraints.contains(constraint)) {
            return
        }

        if (delegatedTo != null) {
            delegatedTo?.addConstraint(constraint)
        } else {
            if (verifyViewIdsUsedByConstraint(constraint)) {
                constraint.constraintItems.map { it.equation }.forEach { ownSolver.addEquation(it) }
                constraint.isManaged = true
            } else {
                // TODO Exception unknown id
            }
        }
        managedConstraints.add(constraint)
    }

    fun removeConstraint(constraint: Constraint) {
        if (!managedConstraints.contains(constraint)) {
            return
        }

        if (delegatedTo != null) {
            delegatedTo?.removeConstraint(constraint)
        } else {
            constraint.constraintItems.map { it.equation }.forEach { ownSolver.removeEquation(it) }
            constraint.isManaged = false
        }
        managedConstraints.remove(constraint)
    }

    fun valueForVariable(variable: ConstraintVariable): Double {
        return delegatedTo?.valueForVariable(variable) ?: ownSolver.valueForVariable(variable)
    }

    fun delegateTo(manager: ConstraintManager) {
        removeDelegate()
        ownSolver = Solver()
        delegatedTo = manager
        managedViews.forEach { delegatedTo?.addManagedView(it) }
        managedConstraints.forEach { delegatedTo?.addConstraint(it) }
    }

    fun stopDelegation() {
        if (delegatedTo == null) {
            return
        }

        removeDelegate()
        managedEquations.flatMap { it.value }.forEach { ownSolver.addEquation(it) }
        managedConstraints.forEach {
            it.constraintItems.map { it.equation }.forEach { ownSolver.addEquation(it) }
            it.isManaged = true
        }
    }

    fun addManagedView(viewId: Int) {
        if (managedViews.contains(viewId)) {
            return
        }

        managedViews.add(viewId)
        val equations = ComplexEquationsProvider(viewId).equations
        managedEquations[viewId] = equations
        if (delegatedTo != null) {
            delegatedTo?.addManagedView(viewId)
        } else {
            equations.forEach { ownSolver.addEquation(it) }
        }
    }

    fun removeManagedView(viewId: Int) {
        if (!managedViews.contains(viewId)) {
            return
        }

        managedViews.remove(viewId)
        val constraints = managedConstraints.filter { !verifyViewIdsUsedByConstraint(it) }
        managedConstraints.removeAll(constraints)
        if (delegatedTo != null) {
            delegatedTo?.removeManagedView(viewId)
        } else {
            managedEquations[viewId]?.forEach { ownSolver.removeEquation(it) }
            constraints.forEach {
                it.constraintItems.map { it.equation }.forEach { ownSolver.removeEquation(it) }
                it.isManaged = false
            }
        }
        managedEquations.remove(viewId)
    }

    fun removeConstraintCreatedFromView(view: View) {
        managedConstraints.filter { it.view == view }.forEach { removeConstraint(it) }
    }

    private fun verifyViewIdsUsedByConstraint(constraint: Constraint): Boolean {
        return constraint.constraintItems.flatMap { it.equation.terms.map { it.variable.viewId } }.all { managedViews.contains(it) }
    }

    private fun removeDelegate() {
        if (delegatedTo == null) {
            return
        }

        managedViews.forEach { delegatedTo?.removeManagedView(it) }
        delegatedTo = null
        val constraints = managedConstraints.filter { !verifyViewIdsUsedByConstraint(it) }
        managedConstraints.removeAll(constraints)
        constraints.forEach { it.isManaged = false }
    }
}