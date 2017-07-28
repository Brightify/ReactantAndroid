package org.brightify.reactant.core.constraint.internal.view

import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ViewConstraints(view: View) {

    val visibilityManager = VisibilityManager(view)
    var intrinsicSizeManager: IntrinsicSizeManager? = null
}