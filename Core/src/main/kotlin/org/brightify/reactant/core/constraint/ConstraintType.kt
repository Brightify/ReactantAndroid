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
        val centerX = ConstraintType("centerX")
        val centerY = ConstraintType("centerY")
        val leading = left // TODO Add support for RTL layouts
        val trailing = right
    }

    override fun toString(): String = description
}
