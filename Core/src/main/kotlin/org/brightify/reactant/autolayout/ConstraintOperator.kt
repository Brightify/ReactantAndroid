package org.brightify.reactant.autolayout

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
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