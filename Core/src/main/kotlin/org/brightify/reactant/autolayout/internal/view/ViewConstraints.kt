package org.brightify.reactant.autolayout.internal.view

import android.view.View
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.internal.AutoLayoutConstraints

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class ViewConstraints(view: View) {

    val visibilityManager = VisibilityManager(view)
    val intrinsicSizeManager: IntrinsicSizeManager?
    val autoLayoutConstraints: AutoLayoutConstraints?

    val usedConstraints: Set<Constraint>
        get() = visibilityManager.usedConstraints +
                (intrinsicSizeManager?.usedConstraints ?: emptySet()) +
                (autoLayoutConstraints?.usedConstraints ?: emptySet())

    init {
        if (view is AutoLayout) {
            intrinsicSizeManager = null
            autoLayoutConstraints = AutoLayoutConstraints(view)
        } else {
            intrinsicSizeManager = IntrinsicSizeManager(view)
            autoLayoutConstraints = null
        }
    }

    fun initialize() {
        autoLayoutConstraints?.initialize()
    }
}