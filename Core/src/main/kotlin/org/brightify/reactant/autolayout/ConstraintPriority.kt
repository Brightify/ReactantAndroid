package org.brightify.reactant.autolayout

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintPriority(value: Int, val description: String = value.toString()) {

    val value: Int = minOf(1000, maxOf(0, value))

    companion object {
        val required = ConstraintPriority(1000, "required")
        val high = ConstraintPriority(750, "high")
        val medium = ConstraintPriority(500, "medium")
        val low = ConstraintPriority(250, "low")
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return value == (other as? ConstraintPriority)?.value ?: -1
    }

    override fun hashCode(): Int {
        return value
    }
}
