package org.brightify.reactant.core.constraint

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintPriority(priority: Int, val description: String = priority.toString()) {

    val priority: Int = minOf(1000, maxOf(0, priority))

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
        return priority == (other as? ConstraintPriority)?.priority ?: -1
    }

    override fun hashCode(): Int {
        return priority
    }
}
