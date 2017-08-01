package org.brightify.reactant.core.util

import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
fun View.assignId() {
    if (id == View.NO_ID) {
        id = View.generateViewId()
    }
}
