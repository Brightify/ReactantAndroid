package org.brightify.reactant.core.constraint.internal.manager

import android.view.View
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.ViewEquationsManager

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal interface ConstraintManager {

    fun addConstraint(constraint: Constraint)

    fun removeConstraint(constraint: Constraint)

    fun removeViewConstraints(view: View)

    fun getValueForVariable(variable: ConstraintVariable): Double

    fun getEquationsManager(view: View): ViewEquationsManager
}