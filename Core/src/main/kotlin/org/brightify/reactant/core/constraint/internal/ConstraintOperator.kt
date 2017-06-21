package org.brightify.reactant.core.constraint.internal

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal enum class ConstraintOperator {
    equal, lessOrEqual, greaterOrEqual;

    override fun toString(): String {
        return when (this) {
            equal -> "="
            lessOrEqual -> "<="
            greaterOrEqual -> ">="
        }
    }
}