package org.brightify.reactant.core.constraint

import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class Margin(open var top: Number = 0, open var left: Number = 0, open var bottom: Number = 0, open var right: Number = 0) {

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

internal class MarginModifier(top: Number, left: Number, bottom: Number, right: Number, onChange: (Margin) -> Unit) : Margin(top, left,
        bottom, right) {

    override var top: Number by Delegates.observable(top) { _, _, _ ->
        onChange(this)
    }

    override var left: Number by Delegates.observable(left) { _, _, _ ->
        onChange(this)
    }

    override var bottom: Number by Delegates.observable(bottom) { _, _, _ ->
        onChange(this)
    }

    override var right: Number by Delegates.observable(right) { _, _, _ ->
        onChange(this)
    }
}