package org.brightify.reactant.core.constraint.internal.manager

import android.view.View
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.ContainerView
import org.brightify.reactant.core.constraint.internal.ViewEquationsManager
import org.brightify.reactant.core.constraint.internal.ViewEquationsManagerWithIntrinsicSize

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ContainerConstraintManager: ConstraintManager {

    val constraints = HashMap<View, HashSet<Constraint>>()

    val equationsManagers = HashMap<View, ViewEquationsManager>()

    override fun addConstraint(constraint: Constraint) {
        if (constraints[constraint.view] == null) {
            constraints[constraint.view] = HashSet()
        }
        constraints[constraint.view]?.add(constraint)
        constraint.isManaged = true
    }

    override fun removeConstraint(constraint: Constraint) {
        constraints[constraint.view]?.add(constraint)
        constraint.isManaged = false
    }

    override fun removeViewConstraints(view: View) {
        constraints[view]?.forEach { removeConstraint(it) }
    }

    override fun getValueForVariable(variable: ConstraintVariable): Double {
        TODO("not implemented")
    }

    override fun getEquationsManager(view: View): ViewEquationsManager {
        var equationsManager = equationsManagers[view]
        if (equationsManager == null) {
            equationsManager = if (view is ContainerView) ViewEquationsManager(view) else ViewEquationsManagerWithIntrinsicSize(view)
            equationsManagers[view] = equationsManager
        }
        return equationsManager
    }
}