package org.brightify.reactant.autolayout.internal.util

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal val Double.isAlmostZero: Boolean
    get() = Math.abs(this) < 1.0e-5