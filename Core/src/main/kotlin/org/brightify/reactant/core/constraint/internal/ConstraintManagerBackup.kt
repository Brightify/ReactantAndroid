//package org.brightify.reactant.core.constraint.internal
//
//import android.view.View
//import org.brightify.reactant.core.constraint.Constraint
//import org.brightify.reactant.core.constraint.ConstraintVariable
//import org.brightify.reactant.core.constraint.exception.ViewNotManagedByCommonAutoLayoutException
//import org.brightify.reactant.core.constraint.internal.manager.ConstraintManager
//import org.brightify.reactant.core.constraint.internal.solver.Equation
//import org.brightify.reactant.core.constraint.internal.solver.Solver
//import org.brightify.reactant.core.constraint.internal.util.DefaultEquationsProvider
//import org.brightify.reactant.core.constraint.internal.util.IntrinsicSize
//
///**
// *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
// */
//internal class ConstraintManagerBackup {
//
//    private var ownSolver = Solver()
//
//    private var delegatedTo: ConstraintManager? = null
//
//    private val managedEquations = HashMap<View, List<Equation>>()
//    private val managedConstraints = HashMap<View, HashSet<Constraint>>()
//    private val managedValueConstraint = HashMap<ConstraintVariable, Constraint>()
//    private val intrinsicSizes = HashMap<View, IntrinsicSize>()
//
//    val allConstraints: List<Constraint>
//        get() = managedConstraints.flatMap { it.value }
//
//    val isDelegated: Boolean
//        get() = delegatedTo != null
//
//    private val managedViews: Set<View>
//        get() = managedEquations.keys
//
//    fun addConstraint(constraint: Constraint) {
//        if (managedConstraints[constraint.view]?.contains(constraint) == true) {
//            return
//        }
//
//        if (isDelegated) {
//            delegatedTo?.addConstraint(constraint)
//        } else {
//            if (verifyViewIdsUsedByConstraint(constraint)) {
//                ownSolver.addConstraint(constraint)
//                constraint.isManaged = true
//            } else {
//                throw ViewNotManagedByCommonAutoLayoutException(constraint.view,
//                        constraint.constraintItems.mapNotNull { it.rightVariable?.view }.first { !managedViews.contains(it) })
//            }
//        }
//
//        if (managedConstraints[constraint.view] == null) {
//            managedConstraints[constraint.view] = HashSet()
//        }
//        managedConstraints[constraint.view]?.add(constraint)
//    }
//
//    fun removeConstraint(constraint: Constraint) {
//        if (managedConstraints[constraint.view]?.contains(constraint) != true) {
//            return
//        }
//
//        if (isDelegated) {
//            delegatedTo?.removeConstraint(constraint)
//        } else {
//            ownSolver.removeConstraint(constraint)
//            constraint.isManaged = false
//        }
//
//        managedConstraints[constraint.view]?.remove(constraint)
//    }
//
//    fun delegateTo(manager: ConstraintManager) {
//        removeDelegate()
//        ownSolver = Solver()
//        delegatedTo = manager
//        managedViews.forEach { delegatedTo?.addManagedView(it) }
//        managedConstraints.flatMap { it.value }.forEach { delegatedTo?.addConstraint(it) }
//        managedValueConstraint.forEach { (variable, constraint) ->
//            addValueConstraint(constraint, variable)
//        }
//    }
//
//    fun stopDelegation() {
//        if (!isDelegated) {
//            return
//        }
//
//        removeDelegate()
//        managedEquations.flatMap { it.value }.forEach { ownSolver.addEquation(it) }
//        managedConstraints.flatMap { it.value }.forEach {
//            ownSolver.addConstraint(it)
//            it.isManaged = true
//        }
//    }
//
//    fun addManagedView(view: View) {
//        if (managedViews.contains(view)) {
//            return
//        }
//
//        val equations = DefaultEquationsProvider(view).equations
//        managedEquations[view] = equations
//        if (isDelegated) {
//            delegatedTo?.addManagedView(view)
//        } else {
//            equations.forEach { ownSolver.addEquation(it) }
//        }
//    }
//
//    fun removeManagedView(view: View) {
//        if (!managedViews.contains(view)) {
//            return
//        }
//
//        // TODO
//        removeManagedViewRecursive(view)
//        managedConstraints.flatMap { it.value }.filter { !verifyViewIdsUsedByConstraint(it) }.forEach { removeConstraint(it) }
//    }
//
//    fun removeConstraintsCreatedFromView(view: View) {
//        // TODO
//        managedConstraints[view]?.forEach { removeConstraint(it) }
//    }
//
//    fun getValueForVariable(variable: ConstraintVariable): Double {
//        return delegatedTo?.getValueForVariable(variable) ?: ownSolver.getValueForVariable(variable)
//    }
//
//    fun setValueForVariable(variable: ConstraintVariable, value: Number) {
//        if (!managedViews.contains(variable.view)) {
//            return
//        }
//
//        var valueConstraint = managedValueConstraint[variable]
//        if (valueConstraint == null) {
//            valueConstraint = Constraint(variable.view, listOf(ConstraintItem(variable, ConstraintOperator.equal)))
//            valueConstraint.initialized = true
//            addConstraint(valueConstraint)
//            addValueConstraint(valueConstraint, variable)
//        }
//        valueConstraint.offset = value
//        valueConstraint.activate()
//    }
//
//    fun resetValueForVariable(variable: ConstraintVariable) {
//        managedValueConstraint[variable]?.deactivate()
//    }
//
//    fun getViewIntrinsicSize(view: View): IntrinsicSize {
//        return intrinsicSizes[view]
//    }
//
//    private fun verifyViewIdsUsedByConstraint(constraint: Constraint): Boolean {
//        return constraint.constraintItems.flatMap { it.equation.terms.map { it.variable.view } }.all { managedViews.contains(it) }
//    }
//
//    private fun removeDelegate() {
//        if (!isDelegated) {
//            return
//        }
//
//        managedViews.forEach { delegatedTo?.removeManagedView(it) }
//        delegatedTo = null
//
//        managedConstraints.flatMap { it.value }.filter { !verifyViewIdsUsedByConstraint(it) }.forEach { removeConstraint(it) }
//    }
//
//    private fun removeManagedViewRecursive(view: View) {
//        if (isDelegated) {
//            delegatedTo?.removeManagedViewRecursive(view)
//        } else {
//            managedEquations[view]?.forEach { ownSolver.removeEquation(it) }
//        }
//
//        managedConstraints.remove(view)
//        managedEquations.remove(view)
//        managedValueConstraint.filter { it.key.view == view }.forEach { (variable, _) -> managedValueConstraint.remove(variable) }
//    }
//
//    private fun addValueConstraint(constraint: Constraint, variable: ConstraintVariable) {
//        if (managedValueConstraint[variable] == null) {
//            managedValueConstraint[variable] = constraint
//        }
//
//        delegatedTo?.addValueConstraint(constraint, variable)
//    }
//}
