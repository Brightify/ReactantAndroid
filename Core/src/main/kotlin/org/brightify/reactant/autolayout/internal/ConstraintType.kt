package org.brightify.reactant.autolayout.internal

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
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
    }

    override fun toString(): String = description
}
