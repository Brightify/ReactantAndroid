package org.brightify.reactant.core.constraint.util

import android.view.View
import android.view.ViewGroup

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
val ViewGroup.children: List<View>
    get() = (0 until childCount).map { getChildAt(it) }
