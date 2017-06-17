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

    val leading: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.leading)

    val trailing: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.trailing)

    val centerX: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerX)

    val centerY: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerY)

    val leftMargin: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.leftMargin)

    val topMargin: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.topMargin)

    val rightMargin: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.rightMargin)

    val bottomMargin: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.bottomMargin)

    val leadingMargin: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.leadingMargin)

    val trailingMargin: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.trailingMargin)

    val centerXWithMargins: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerXWithMargins)

    val centerYWithMargins: ConstraintMaker
        get() = ConstraintMaker(ConstraintType.centerYWithMargins)

    val edges: ConstraintMaker
        get() = left.top.right.bottom

    val size: ConstraintMaker
        get() = width.height

    val center: ConstraintMaker
        get() = centerX.centerY

    val margins: ConstraintMaker
        get() = leftMargin.topMargin.rightMargin.bottomMargin

    val centerWithMargins: ConstraintMaker
        get() = centerXWithMargins.centerYWithMargins

    protected open fun ConstraintMaker(type: ConstraintType): ConstraintMaker {
        return ConstraintMaker(view, createdConstraints, listOf(type))
    }
}