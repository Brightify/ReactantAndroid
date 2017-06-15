package org.brightify.reactant.core.constraint

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintType private constructor(val value: Int) {

    companion object {
        val none = ConstraintType(0)

        val left = ConstraintType(1 shl 0)
        val top = ConstraintType(1 shl 1)
        val right = ConstraintType(1 shl 2)
        val bottom = ConstraintType(1 shl 3)
        val width = ConstraintType(1 shl 4)
        val height = ConstraintType(1 shl 5)
        val centerX = ConstraintType(1 shl 6)
        val centerY = ConstraintType(1 shl 7)
        val leading = left // TODO Add support for RTL layouts
        val trailing = right

        val edges = ConstraintType(left, top, right, bottom)
        val size = ConstraintType(width, height)
        val center = ConstraintType(centerX, centerY)

        private val baseTypes = arrayOf(left, top, right, bottom, width, height, centerX, centerY)
    }

    constructor(vararg types: ConstraintType): this(types.map { it.value }.reduce { acc, value -> acc or value })

    fun toBaseTypes(): List<ConstraintType> {
        val list = ArrayList<ConstraintType>()
        baseTypes.forEach {
            if (value and it.value == it.value) {
                list.add(it)
            }
        }
        return list
    }

    override fun toString(): String {
        return toBaseTypes().map {
            when(it) {
                left -> "left"
                top -> "top"
                right -> "right"
                bottom -> "bottom"
                width -> "width"
                height -> "height"
                centerX -> "centerX"
                centerY -> "centerY"
                else -> ""
            }
        }.joinToString(", ")
    }

    override fun equals(other: Any?): Boolean {
        return value == (other as? ConstraintType)?.value ?: -1
    }

    override fun hashCode(): Int {
        return value
    }
}