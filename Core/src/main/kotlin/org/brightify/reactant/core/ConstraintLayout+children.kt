package org.brightify.reactant.core

import android.support.constraint.ConstraintLayout
import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
fun <T: ConstraintLayout> T.children(vararg children: View): T {
    children.forEach(this::addView)
    return this
}
