package org.brightify.reactant.autolayout.util

import android.view.View
import android.view.ViewGroup

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
val ViewGroup.children: List<View>
    get() = (0 until childCount).map { getChildAt(it) }

inline fun ViewGroup.forEachChild(action: (View) -> Unit) {
    (0 until childCount).forEach {
        action(getChildAt(it))
    }
}

fun <T : ViewGroup> T.children(vararg children: View?): T {
    children.filterNotNull().forEach(this::addView)
    return this
}
