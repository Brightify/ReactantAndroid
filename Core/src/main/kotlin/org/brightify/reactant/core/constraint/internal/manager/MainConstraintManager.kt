package org.brightify.reactant.core.constraint.internal.manager

import android.view.View
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.ContainerView
import org.brightify.reactant.core.constraint.exception.ViewNotManagedByCommonAutoLayoutException
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.internal.ViewEquationsManager
import org.brightify.reactant.core.constraint.internal.ViewEquationsManagerWithIntrinsicSize
import org.brightify.reactant.core.constraint.internal.solver.Equation
import org.brightify.reactant.core.constraint.internal.solver.Solver

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
// TODO Exceptions
internal class MainConstraintManager : ConstraintManager {

    private val solver = Solver()
    private val constraints = HashMap<View, HashSet<Constraint>>()
    private val valueForVariable = HashMap<ConstraintVariable, Equation>()
    private val equationsManagers = HashMap<View, ViewEquationsManager>()

    private val managedViews: Set<View>
        get() = equationsManagers.keys

    val allConstraints: List<Constraint>
        get() = constraints.flatMap { it.value }

    override fun addConstraint(constraint: Constraint) {
        if (constraints[constraint.view]?.contains(constraint) == true) {
            return
        }

        if (verifyViewsUsedByConstraint(constraint)) {
            solver.addConstraint(constraint)
            constraint.isManaged = true
        } else {
            throw ViewNotManagedByCommonAutoLayoutException(constraint.view,
                    constraint.constraintItems.mapNotNull { it.rightVariable?.view }.first { !managedViews.contains(it) })
        }

        if (constraints[constraint.view] == null) {
            constraints[constraint.view] = HashSet()
        }
        constraints[constraint.view]?.add(constraint)
    }

    override fun removeConstraint(constraint: Constraint) {
        if (constraints[constraint.view]?.remove(constraint) != true) {
            return
        }

        solver.removeConstraint(constraint)
        constraint.isManaged = false
    }

    fun solve() {
        solver.solve()
    }

    fun addManagedView(view: View) {
        if (managedViews.contains(view)) {
            return
        }

        val equationsManager = if (view is ContainerView || view is AutoLayout) ViewEquationsManager(
                view) else ViewEquationsManagerWithIntrinsicSize(view)
        equationsManager.addEquations(solver)
        equationsManagers[view] = equationsManager
    }

    fun removeManagedView(view: View) {
        if (!managedViews.contains(view)) {
            return
        }

        constraints.flatMap { it.value }.filter { !verifyViewsUsedByConstraint(it) }.forEach { removeConstraint(it) }
        constraints.remove(view)

        valueForVariable.filter { it.key.view == view }.forEach { (variable, _) -> resetValueForVariable(variable) }

        equationsManagers.remove(view)?.removeEquations()
    }

    fun addAll(containerConstraintManagers: Set<ContainerConstraintManager>) {
        containerConstraintManagers.forEach {
            it.equationsManagers.forEach {
                if (!managedViews.contains(it.key)) {
                    // TODO
                }
                equationsManagers[it.key]?.removeEquations()
                it.value.addEquations(solver)
                equationsManagers[it.key] = it.value
            }
        }
        containerConstraintManagers.forEach { it.constraints.forEach { it.value.forEach { addConstraint(it) } } }
    }

    override fun removeViewConstraints(view: View) {
        constraints[view]?.forEach { removeConstraint(it) }
    }

    override fun getValueForVariable(variable: ConstraintVariable): Double {
        return ConstraintType.termsForVariable(variable, 1.0).map { it.coefficient * solver.getValueForVariable(it.variable) }.reduce { acc, term -> acc + term }
    }

    fun setValueForVariable(variable: ConstraintVariable, value: Number) {
        if (!managedViews.contains(variable.view)) {
            return
        }

        val oldEquation = valueForVariable[variable]
        if (oldEquation?.constant == value) {
            return
        }

        oldEquation?.let { solver.removeEquation(it) }
        val equation = Equation(ConstraintType.termsForVariable(variable, 1.0), constant = value.toDouble())
        solver.addEquation(equation)
        valueForVariable[variable] = equation
    }

    fun resetValueForVariable(variable: ConstraintVariable) {
        valueForVariable.remove(variable)?.let { solver.removeEquation(it) }
    }

    // TODO WithIntrinsicSize
    override fun getEquationsManager(view: View): ViewEquationsManager = equationsManagers[view] ?: throw RuntimeException(
            "View is not managed.")

    private fun verifyViewsUsedByConstraint(constraint: Constraint, views: Set<View> = managedViews): Boolean {
        constraint.constraintItems.forEach {
            it.equation.terms.forEach {
                if (!views.contains(it.variable.view)) {
                    return false
                }
            }
        }
        return true
    }
}
