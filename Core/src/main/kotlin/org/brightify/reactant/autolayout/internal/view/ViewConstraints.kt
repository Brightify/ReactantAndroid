package org.brightify.reactant.autolayout.internal.view

import android.view.View
import org.brightify.reactant.autolayout.Constraint

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class ViewConstraints(view: View) {

    val visibilityManager = VisibilityManager(view)
    var intrinsicSizeManager: IntrinsicSizeManager? = null

    val usedConstraints: Set<Constraint>
        get() = visibilityManager.usedConstraints + (intrinsicSizeManager?.usedConstraints ?: emptySet())
}