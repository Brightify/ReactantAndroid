package org.brightify.reactant.core.constraint.internal.intrinsicsize

import android.view.View
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.internal.solver.Solver
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class IntrinsicSizeManager(view: View, solver: Solver) {

    val width = IntrinsicDimensionManager(solver, ConstraintVariable(view, ConstraintType.width))
    val height = IntrinsicDimensionManager(solver, ConstraintVariable(view, ConstraintType.height))

    var solver by Delegates.observable(solver) { _, _, newValue ->
        width.solver = newValue
        height.solver = newValue
    }

    fun addEquations() {
        width.addEquations()
        height.addEquations()
    }

    fun removeEquations() {
        width.removeEquations()
        height.removeEquations()
    }
}