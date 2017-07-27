package org.brightify.reactant.core.constraint.internal.util

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal val Double.isAlmostZero: Boolean
    get() = Math.abs(this) < 1.0e-8