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
    private val managedViews = HashSet<View>()

    val isDelegated: Boolean
        get() = delegatedTo != null

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

    fun valueForVariable(variable: ConstraintVariable): Double {
        return delegatedTo?.valueForVariable(variable) ?: ownSolver.valueForVariable(variable)
    }

    fun delegateTo(manager: ConstraintManager) {
        removeDelegate()
        ownSolver = Solver()
        delegatedTo = manager
        managedViews.forEach { delegatedTo?.addManagedView(it) }
        managedConstraints.flatMap { it.value }.forEach { delegatedTo?.addConstraint(it) }
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

        managedViews.add(view)
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

        managedViews.remove(view)

        if (isDelegated) {
            delegatedTo?.removeManagedView(view)
        } else {
            managedEquations[view]?.forEach { ownSolver.removeEquation(it) }
            managedConstraints.forEach { _, constraints ->
                val constraintsToRemove = constraints.filter { !verifyViewIdsUsedByConstraint(it) }
                constraints.removeAll(constraintsToRemove)
                constraintsToRemove.forEach {
                    it.constraintItems.map { it.equation }.forEach { ownSolver.removeEquation(it) }
                    it.isManaged = false
                }
            }
        }

        managedConstraints.remove(view)
        managedEquations.remove(view)
    }

    fun removeConstraintCreatedFromView(view: View) {
        managedConstraints[view]?.forEach { removeConstraint(it) }
    }

    // TODO Rewrite
    fun getValueConstraint(variable: ConstraintVariable): Constraint {
        val constraint = managedConstraints[variable.view]?.firstOrNull {
            it.view == variable.view && it.constraintItems.map { it.leftVariable }.contains(variable)
        }
        if (constraint != null) {
            return constraint
        } else {
            val newConstraint = Constraint(variable.view, listOf(
                    ConstraintItem(ConstraintVariable(variable.view, variable.type), ConstraintOperator.equal, offset = 0)
            ))
            addConstraint(newConstraint)
            return newConstraint
        }
    }

    fun updateIntrinsicSizes(density: Float) {
        managedViews.forEach { view ->
            var width = managedConstraints[view]?.firstOrNull {
                it.view == view && it.constraintItems.map { it.leftVariable.type }.contains(
                        ConstraintType.intrinsicWidth)
            }
            var height = managedConstraints[view]?.firstOrNull {
                it.view == view && it.constraintItems.map { it.leftVariable.type }.contains(
                        ConstraintType.intrinsicHeight)
            }

            if (width == null) {
                width = Constraint(view, listOf(
                        ConstraintItem(ConstraintVariable(view, ConstraintType.intrinsicWidth), ConstraintOperator.equal, offset = 0)
                ))
                addConstraint(width)
            }
            if (height == null) {
                height = Constraint(view, listOf(
                        ConstraintItem(ConstraintVariable(view, ConstraintType.intrinsicHeight), ConstraintOperator.equal, offset = 0)
                ))
                addConstraint(height)
            }
            width.offset = view.measuredWidth / density
            height.offset = view.measuredHeight / density
        }
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

        managedConstraints.forEach { _, constraints ->
            val constraintsToRemove = constraints.filter { !verifyViewIdsUsedByConstraint(it) }
            constraints.removeAll(constraintsToRemove)
            constraintsToRemove.forEach { it.isManaged = false }
        }
    }
}