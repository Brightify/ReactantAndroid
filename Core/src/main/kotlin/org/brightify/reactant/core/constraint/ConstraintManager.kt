package org.brightify.reactant.core.constraint

import android.view.View
import org.brightify.reactant.core.constraint.solver.Equation
import org.brightify.reactant.core.constraint.solver.Solver
import org.brightify.reactant.core.constraint.util.DefaultEquationsProvider

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ConstraintManager {

    private var ownSolver = Solver()

    private var delegatedTo: ConstraintManager? = null

    private val managedEquations = HashMap<View, List<Equation>>()
    private val managedConstraints = HashMap<View, HashSet<Constraint>>()
    private val managedValueConstraint = HashMap<View, HashMap<ConstraintVariable, Constraint>>()

    val isDelegated: Boolean
        get() = delegatedTo != null

    val managedViews: Set<View>
        get() = managedEquations.keys

    fun addConstraint(constraint: Constraint) {
        if (managedConstraints[constraint.view]?.contains(constraint) == true) {
            return
        }

        if (isDelegated) {
            delegatedTo?.addConstraint(constraint)
        } else {
            if (verifyViewIdsUsedByConstraint(constraint)) {
                constraint.constraintItems.map { it.equation }.forEach { ownSolver.addEquation(it) }
                constraint.isManaged = true
            } else {
                // TODO Exception unknown id
            }
        }

        if (managedConstraints[constraint.view] == null) {
            managedConstraints[constraint.view] = HashSet()
        }
        managedConstraints[constraint.view]?.add(constraint)
    }

    fun removeConstraint(constraint: Constraint) {
        if (managedConstraints[constraint.view]?.contains(constraint) != true) {
            return
        }

        if (isDelegated) {
            delegatedTo?.removeConstraint(constraint)
        } else {
            constraint.constraintItems.map { it.equation }.forEach { ownSolver.removeEquation(it) }
            constraint.isManaged = false
        }

        managedConstraints[constraint.view]?.remove(constraint)
    }

    fun delegateTo(manager: ConstraintManager) {
        removeDelegate()
        ownSolver = Solver()
        delegatedTo = manager
        managedViews.forEach { delegatedTo?.addManagedView(it) }
        managedConstraints.flatMap { it.value }.forEach { delegatedTo?.addConstraint(it) }
        managedValueConstraint.forEach { (_, constraints) ->
            constraints.forEach { (variable, constraint) ->
                addValueConstraint(constraint, variable)
            }
        }
    }

    fun stopDelegation() {
        if (!isDelegated) {
            return
        }

        removeDelegate()
        managedEquations.flatMap { it.value }.forEach { ownSolver.addEquation(it) }
        managedConstraints.flatMap { it.value }.forEach {
            it.constraintItems.map { it.equation }.forEach { ownSolver.addEquation(it) }
            it.isManaged = true
        }
    }

    fun addManagedView(view: View) {
        if (managedViews.contains(view)) {
            return
        }

        val equations = DefaultEquationsProvider(view).equations
        managedEquations[view] = equations
        if (isDelegated) {
            delegatedTo?.addManagedView(view)
        } else {
            equations.forEach { ownSolver.addEquation(it) }
        }
    }

    fun removeManagedView(view: View) {
        if (!managedViews.contains(view)) {
            return
        }

        removeConstraintCreatedFromView(view)
        managedConstraints.flatMap { it.value }.filter { !verifyViewIdsUsedByConstraint(it) }.forEach { removeConstraint(it) }

        removeManagedViewRecursive(view)
    }

    fun removeConstraintCreatedFromView(view: View) {
        managedConstraints[view]?.forEach { removeConstraint(it) }
    }

    fun valueForVariable(variable: ConstraintVariable): Double {
        return delegatedTo?.valueForVariable(variable) ?: ownSolver.valueForVariable(variable)
    }

    fun setValueForVariable(variable: ConstraintVariable, value: Number) {
        if (!managedViews.contains(variable.view)) {
            return
        }

        var valueConstraint = managedValueConstraint[variable.view]?.get(variable)
        if (valueConstraint == null) {
            valueConstraint = Constraint(variable.view, listOf(ConstraintItem(variable, ConstraintOperator.equal)))
            addConstraint(valueConstraint)
            addValueConstraint(valueConstraint, variable)
        }
        valueConstraint.offset = value
    }

    fun resetValueForVariable(variable: ConstraintVariable) {
        managedValueConstraint[variable.view]?.get(variable)?.let { removeConstraint(it) }
        removeValueConstraint(variable)
    }

    private fun verifyViewIdsUsedByConstraint(constraint: Constraint): Boolean {
        return constraint.constraintItems.flatMap { it.equation.terms.map { it.variable.view } }.all { managedViews.contains(it) }
    }

    private fun removeDelegate() {
        if (!isDelegated) {
            return
        }

        managedViews.forEach { delegatedTo?.removeManagedView(it) }
        delegatedTo = null

        managedConstraints.flatMap { it.value }.filter { !verifyViewIdsUsedByConstraint(it) }.forEach { removeConstraint(it) }
    }

    private fun removeManagedViewRecursive(view: View) {
        if (isDelegated) {
            delegatedTo?.removeManagedViewRecursive(view)
        } else {
            managedEquations[view]?.forEach { ownSolver.removeEquation(it) }
        }

        managedConstraints.remove(view)
        managedEquations.remove(view)
        managedValueConstraint.remove(view)
    }

    private fun addValueConstraint(constraint: Constraint, variable: ConstraintVariable) {
        if (managedValueConstraint[variable.view] == null) {
            managedValueConstraint[variable.view] = HashMap()
        }
        if (managedValueConstraint[variable.view]?.get(variable) == null) {
            managedValueConstraint[variable.view]?.set(variable, constraint)
        }

        delegatedTo?.addValueConstraint(constraint, variable)
    }

    private fun removeValueConstraint(variable: ConstraintVariable) {
        managedValueConstraint[variable.view]?.remove(variable)

        delegatedTo?.removeValueConstraint(variable)
    }
}
