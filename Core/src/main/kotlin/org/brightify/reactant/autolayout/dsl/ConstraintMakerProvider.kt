package org.brightify.reactant.autolayout.dsl

import android.view.View
import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.internal.ConstraintType

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ConstraintMakerProvider internal constructor(protected val view: View,
                                                        protected val createdConstraints: MutableList<Constraint>) {

    val left: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.left)

    val top: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.top)

    val right: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.right)

    val bottom: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.bottom)

    val width: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.width)

    val height: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.height)

    // TODO Add support for RTL layouts
    val leading: ConstraintMaker
        get() = left

    val trailing: ConstraintMaker
        get() = right

    val centerX: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerX)

    val centerY: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerY)

    val edges: ConstraintMaker
        get() = left.top.right.bottom

    val size: ConstraintMaker
        get() = width.height

    val center: ConstraintMaker
        get() = centerX.centerY

    internal open fun ConstraintMaker(type: ConstraintType): ConstraintMaker {
        return ConstraintMaker(view, createdConstraints, listOf(type))
    }
}