package org.brightify.reactant.core.util

import android.content.Context
import android.content.res.Configuration

/**
 * @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
val Context.inLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.inPortrait: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT