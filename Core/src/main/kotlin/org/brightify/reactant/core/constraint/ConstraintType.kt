package org.brightify.reactant.core.constraint

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintType private constructor(private val description: String) {

    companion object {
        val left = ConstraintType("left")
        val top = ConstraintType("top")
        val right = ConstraintType("right")
        val bottom = ConstraintType("bottom")
        val width = ConstraintType("width")
        val height = ConstraintType("height")
        val leading = left // TODO Add support for RTL layouts
        val trailing = right
        val centerX = ConstraintType("centerX")
        val centerY = ConstraintType("centerY")
        val leftMargin = ConstraintType("leftMargin")
        val topMargin = ConstraintType("topMargin")
        val rightMargin = ConstraintType("rightMargin")
        val bottomMargin = ConstraintType("bottomMargin")
        val leadingMargin = leftMargin
        val trailingMargin = rightMargin
        val centerXWithMargins = ConstraintType("centerXWithMargins")
        val centerYWithMargins = ConstraintType("centerYWithMargins")

        internal val leftMarginSize = ConstraintType("leftMarginSize")
        internal val topMarginSize = ConstraintType("topMarginSize")
        internal val rightMarginSize = ConstraintType("rightMarginSize")
        internal val bottomMarginSize = ConstraintType("bottomMarginSize")
    }

    override fun toString(): String = description
}
