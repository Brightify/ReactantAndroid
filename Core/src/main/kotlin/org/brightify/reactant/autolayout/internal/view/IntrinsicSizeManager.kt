package org.brightify.reactant.autolayout.internal.view

import android.view.View
import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.autolayout.internal.ConstraintType

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class IntrinsicSizeManager(view: View) {

    val width = IntrinsicDimensionManager(ConstraintVariable(view, ConstraintType.width))
    val height = IntrinsicDimensionManager(ConstraintVariable(view, ConstraintType.height))

    val usedConstraints: Set<Constraint>
        get() = width.usedConstraints + height.usedConstraints
}