package org.brightify.reactant.core.constraint.internal.manager

import android.view.View
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.util.IntrinsicSize

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class DelegatedConstraintManager(private val autoLayout: AutoLayout) : ConstraintManager {

    override val mainConstraintManager: MainConstraintManager
        get() = autoLayout.constraintManager.mainConstraintManager

    override val allConstraints: List<Constraint>
        get() = autoLayout.constraintManager.allConstraints

    override fun addConstraint(constraint: Constraint) = autoLayout.constraintManager.addConstraint(constraint)

    override fun removeConstraint(constraint: Constraint) = autoLayout.constraintManager.removeConstraint(constraint)

    override fun addManagedView(view: View) = autoLayout.constraintManager.addManagedView(view)

    override fun removeManagedView(view: View) = autoLayout.constraintManager.removeManagedView(view)

    override fun removeViewConstraintsCreatedByUser(view: View) = autoLayout.constraintManager.removeViewConstraintsCreatedByUser(view)

    override fun getValueForVariable(variable: ConstraintVariable): Double = autoLayout.constraintManager.getValueForVariable(variable)

    override fun setValueForVariable(variable: ConstraintVariable, value: Number) = autoLayout.constraintManager.setValueForVariable(
            variable, value)

    override fun resetValueForVariable(variable: ConstraintVariable) = autoLayout.constraintManager.resetValueForVariable(variable)

    override fun getViewIntrinsicSize(view: View): IntrinsicSize = autoLayout.constraintManager.getViewIntrinsicSize(view)

    override fun addAllToManager(manager: ConstraintManager) = autoLayout.constraintManager.addAllToManager(manager)

    override fun splitToMainManagerForAutoLayout(
            layout: AutoLayout): MainConstraintManager = autoLayout.constraintManager.splitToMainManagerForAutoLayout(layout)
}
