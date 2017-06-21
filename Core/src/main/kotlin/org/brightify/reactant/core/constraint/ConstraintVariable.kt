package org.brightify.reactant.core.constraint

import android.view.View
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.util.description

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintVariable internal constructor(internal val view: View, internal val type: ConstraintType) {

    override fun toString(): String {
        return "${view.description}.$type"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConstraintVariable) return false

        if (view != other.view) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = view.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}