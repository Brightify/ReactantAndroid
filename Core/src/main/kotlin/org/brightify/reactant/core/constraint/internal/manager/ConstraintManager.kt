package org.brightify.reactant.core.constraint.internal.manager

import android.view.View
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.util.IntrinsicSize

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal interface ConstraintManager {

    val mainConstraintManager: MainConstraintManager

    val allConstraints: List<Constraint>

    fun addConstraint(constraint: Constraint)

    fun removeConstraint(constraint: Constraint)

    fun addManagedView(view: View)

    fun removeManagedView(view: View)

    fun removeViewConstraintsCreatedByUser(view: View)

    fun getValueForVariable(variable: ConstraintVariable): Double

    fun setValueForVariable(variable: ConstraintVariable, value: Number)

    fun resetValueForVariable(variable: ConstraintVariable)

    fun getViewIntrinsicSize(view: View): IntrinsicSize

    fun addAllToManager(manager: ConstraintManager)

    fun splitToMainManagerForAutoLayout(layout: AutoLayout): MainConstraintManager
}