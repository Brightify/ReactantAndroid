package org.brightify.reactant.core.constraint.internal

import android.view.View
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.exception.ViewNotManagedByCommonAutoLayoutException
import org.brightify.reactant.core.constraint.internal.intrinsicsize.IntrinsicSizeManager
import org.brightify.reactant.core.constraint.internal.intrinsicsize.IntrinsicSizeNecessityDecider
import org.brightify.reactant.core.constraint.internal.solver.Solver
import org.brightify.reactant.core.constraint.internal.solver.Term
import org.brightify.reactant.core.constraint.util.children

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ConstraintManager {

    private val solver = Solver()
    private val constraints = HashMap<View, HashSet<Constraint>>()
    private val intrinsicSizeManagers = HashMap<View, IntrinsicSizeManager>()
    private val intrinsicSizeNecessityDecider = IntrinsicSizeNecessityDecider()

    private val managedViews: Set<View>
        get() = constraints.keys

    val allConstraints: List<Constraint>
        get() = constraints.flatMap { it.value }

    fun addConstraint(constraint: Constraint) {
        if (constraints[constraint.view]?.contains(constraint) == true) {
            return
        }

        if (verifyViewsUsedByConstraint(constraint)) {
            solver.addConstraint(constraint)
            constraint.isManaged = true
            constraints[constraint.view]?.add(constraint)
            intrinsicSizeNecessityDecider.addConstraint(constraint)
        } else {
            throw ViewNotManagedByCommonAutoLayoutException(constraint.view,
                    constraint.constraintItems.mapNotNull { it.rightVariable?.view }.first { !managedViews.contains(it) })
        }
    }

    fun removeConstraint(constraint: Constraint) {
        if (constraints[constraint.view]?.remove(constraint) != true) {
            return
        }

        solver.removeConstraint(constraint)
        constraint.isManaged = false
        intrinsicSizeNecessityDecider.removeConstraint(constraint)
    }

    fun solve() {
        solver.solve()
    }

    fun addManagedView(view: View) {
        if (managedViews.contains(view)) {
            return
        }

        constraints[view] = HashSet()
        if (view !is AutoLayout) {
            intrinsicSizeManagers[view] = IntrinsicSizeManager(view, solver)
        }
    }

    fun removeManagedView(view: View) {
        if (!managedViews.contains(view)) {
            return
        }

        constraints.remove(view)?.forEach { removeConstraint(it) }
        normalizeConstraints()

        intrinsicSizeManagers.remove(view)?.removeEquations()
    }

    fun join(constraintManager: ConstraintManager) {
        constraints.putAll(constraintManager.constraints)
        constraintManager.constraints.forEach {
            it.value.forEach { constraint ->
                solver.addConstraint(constraint)
                intrinsicSizeNecessityDecider.addConstraint(constraint)
            }
        }
        intrinsicSizeManagers.putAll(constraintManager.intrinsicSizeManagers)
        constraintManager.intrinsicSizeManagers.forEach {
            it.value.solver = solver
            it.value.addEquations()
        }
    }

    fun split(view: View): ConstraintManager {
        val leavingViews = HashSet<View>()
        fun addRecursive(leaving: View) {
            leavingViews.add(leaving)
            if (leaving is AutoLayout) {
                leaving.children.forEach {
                    addRecursive(it)
                }
            }
        }
        addRecursive(view)

        val newConstraintManager = ConstraintManager()

        val leavingConstraints = constraints.filterKeys { leavingViews.contains(it) }
        newConstraintManager.constraints.putAll(leavingConstraints)
        newConstraintManager.normalizeConstraints()
        newConstraintManager.solve()
        newConstraintManager.constraints.forEach {
            it.value.forEach {
                newConstraintManager.solver.addConstraint(it)
            }
        }

        leavingViews.forEach {
            constraints.remove(it)?.forEach {
                solver.removeConstraint(it)
            }
        }
        normalizeConstraints()

        val leavingSizeManagers = intrinsicSizeManagers.filterKeys { leavingViews.contains(it) }
        newConstraintManager.intrinsicSizeManagers.putAll(leavingSizeManagers)
        leavingSizeManagers.forEach {
            intrinsicSizeManagers.remove(it.key)
            it.value.removeEquations()
            it.value.solver = newConstraintManager.solver
            it.value.addEquations()
        }

        return newConstraintManager
    }

    fun removeViewConstraints(view: View) {
        constraints[view]?.forEach { removeConstraint(it) }
    }

    fun getValueForVariable(variable: ConstraintVariable): Double {
        var result = 0.0
        Term(variable).baseTerms.forEach {
            result += it.coefficient * solver.getValueForVariable(it.variable)
        }
        return result
    }

    fun getIntrinsicSizeManager(view: View): IntrinsicSizeManager? = intrinsicSizeManagers[view]

    fun disableIntrinsicSize(view: View) {
        intrinsicSizeManagers.remove(view)?.removeEquations()
    }

    fun needsIntrinsicWidth(view: View): Boolean = intrinsicSizeNecessityDecider.needsIntrinsicWidth(view)

    fun needsIntrinsicHeight(view: View): Boolean = intrinsicSizeNecessityDecider.needsIntrinsicHeight(view)

    private fun verifyViewsUsedByConstraint(constraint: Constraint): Boolean {
        return constraint.constraintItems.all {
            managedViews.contains(it.leftVariable.view) && it.rightVariable?.let { managedViews.contains(it.view) } != false
        }
    }

    private fun normalizeConstraints() {
        constraints.forEach {
            it.value.forEach {
                if (!verifyViewsUsedByConstraint(it)) {
                    removeConstraint(it)
                }
            }
        }
    }
}
