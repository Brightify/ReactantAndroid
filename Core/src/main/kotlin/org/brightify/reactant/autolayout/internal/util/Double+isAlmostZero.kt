package org.brightify.reactant.autolayout.internal.util

import kotlin.math.abs

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal val Double.isAlmostZero: Boolean
    get() = abs(this) < 1.0e-5