package org.brightify.reactant.core.constraint

import android.view.View

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

    val centerX: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerX)

    val centerY: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerY)

    val leading: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.leading)

    val trailing: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.trailing)

    val edges: ConstraintMaker
        get() = left.top.right.bottom

    val size: ConstraintMaker
        get() = width.height

    val center: ConstraintMaker
        get() = centerX.centerY

    protected open fun ConstraintMaker(type: ConstraintType): ConstraintMaker {
        return ConstraintMaker(view, createdConstraints, listOf(type))
    }
}