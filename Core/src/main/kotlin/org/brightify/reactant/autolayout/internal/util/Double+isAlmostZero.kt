package org.brightify.reactant.autolayout.internal.util

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal val Double.isAlmostZero: Boolean
    get() = Math.abs(this) < 1.0e-5