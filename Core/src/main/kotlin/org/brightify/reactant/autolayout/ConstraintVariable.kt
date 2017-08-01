package org.brightify.reactant.autolayout

import android.view.View
import org.brightify.reactant.autolayout.internal.ConstraintType
import org.brightify.reactant.autolayout.util.description

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

        return view == other.view && type == other.type
    }

    override fun hashCode(): Int = 31 * view.hashCode() + type.hashCode()
}