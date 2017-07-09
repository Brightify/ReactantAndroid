package org.brightify.reactant.core.constraint.util

import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class Margin(top: Number = 0, left: Number = 0, bottom: Number = 0, right: Number = 0) {

    internal var onChange: ((Margin) -> Unit)? = null

    var top: Number by onChange(top) { _, _, _ ->
        onChange?.invoke(this)
    }

    var left: Number by onChange(left) { _, _, _ ->
        onChange?.invoke(this)
    }

    var bottom: Number by onChange(bottom) { _, _, _ ->
        onChange?.invoke(this)
    }

    var right: Number by onChange(right) { _, _, _ ->
        onChange?.invoke(this)
    }

    constructor(value: Number) : this(value, value, value, value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Margin) return false

        if (top != other.top) return false
        if (left != other.left) return false
        if (bottom != other.bottom) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = top.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + bottom.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

    override fun toString(): String {
        return "top: $top, left: $left, bottom: $bottom, right: $right"
    }
}
