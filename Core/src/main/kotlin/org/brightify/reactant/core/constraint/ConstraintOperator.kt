package org.brightify.reactant.core.constraint

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
enum class ConstraintOperator {
    equal, lessOrEqual, greaterOrEqual;

    override fun toString(): String {
        return when (this) {
            equal -> "="
            lessOrEqual -> "<="
            greaterOrEqual -> ">="
        }
    }
}