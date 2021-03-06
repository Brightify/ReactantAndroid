package org.brightify.reactant.autolayout

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class ConstraintPriority(value: Int, val description: String = value.toString()) {

    val value: Int = minOf(1000, maxOf(0, value))

    companion object {
        val required = ConstraintPriority(1000, "required")
        val visibility = ConstraintPriority(900, "visibility")
        val high = ConstraintPriority(750, "high")
        val medium = ConstraintPriority(500, "medium")
        val low = ConstraintPriority(250, "low")
        val autoLayoutIntrinsicSize = ConstraintPriority(100, "autolayout")
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
