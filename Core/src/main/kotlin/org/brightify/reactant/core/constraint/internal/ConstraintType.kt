package org.brightify.reactant.core.constraint.internal

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ConstraintType private constructor(private val description: String) {

    companion object {
        val left = ConstraintType("left")
        val top = ConstraintType("top")
        val right = ConstraintType("right")
        val bottom = ConstraintType("bottom")
        val width = ConstraintType("width")
        val height = ConstraintType("height")
        val centerX = ConstraintType("centerX")
        val centerY = ConstraintType("centerY")
        val leftMargin = ConstraintType("leftMargin")
        val topMargin = ConstraintType("topMargin")
        val rightMargin = ConstraintType("rightMargin")
        val bottomMargin = ConstraintType("bottomMargin")
        val centerXWithMargins = ConstraintType("centerXWithMargins")
        val centerYWithMargins = ConstraintType("centerYWithMargins")

        val leftMarginSize = ConstraintType("leftMarginSize")
        val topMarginSize = ConstraintType("topMarginSize")
        val rightMarginSize = ConstraintType("rightMarginSize")
        val bottomMarginSize = ConstraintType("bottomMarginSize")
        val intrinsicWidth = ConstraintType("intrinsicWidth")
        val intrinsicHeight = ConstraintType("intrinsicHeight")
    }

    override fun toString(): String = description
}
